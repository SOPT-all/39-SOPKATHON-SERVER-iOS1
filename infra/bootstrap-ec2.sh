#!/usr/bin/env bash
set -euo pipefail

infra_dir=$(cd -- "$(dirname -- "$0")" && pwd)
export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y --no-install-recommends nginx openjdk-21-jre-headless mysql-client ec2-instance-connect

getent passwd sopkathon-app >/dev/null || useradd --system --user-group --shell /usr/sbin/nologin sopkathon-app
getent passwd sopkathon-deploy >/dev/null || useradd --create-home --shell /bin/bash sopkathon-deploy
install -d -m 0755 /etc/sopkathon /opt/sopkathon
install -m 0755 "$infra_dir/deploy.sh" /usr/local/sbin/sopkathon-deploy
install -m 0755 "$infra_dir/init-db.sh" /usr/local/sbin/sopkathon-init-db

tmp_dir=$(mktemp -d)
trap 'rm -rf -- "$tmp_dir"' EXIT
curl -fsSL --retry 3 https://truststore.pki.rds.amazonaws.com/ap-northeast-2/ap-northeast-2-bundle.pem -o "$tmp_dir/rds-ca-bundle.pem"
awk '/BEGIN CERTIFICATE/{n++} n{print > ("'"$tmp_dir"'/ca-" n ".pem")}' "$tmp_dir/rds-ca-bundle.pem"
for cert in "$tmp_dir"/ca-*.pem; do
    keytool -importcert -noprompt -alias "$(basename "$cert")" -file "$cert" \
        -keystore "$tmp_dir/rds-truststore.p12" -storetype PKCS12 -storepass changeit
done
install -m 0644 "$tmp_dir/rds-ca-bundle.pem" "$tmp_dir/rds-truststore.p12" /etc/sopkathon

cat > "$tmp_dir/sudoers" <<'EOF'
sopkathon-deploy ALL=(root) NOPASSWD: /usr/local/sbin/sopkathon-deploy ""
EOF
chmod 0440 "$tmp_dir/sudoers"
visudo -cf "$tmp_dir/sudoers"
install -m 0440 "$tmp_dir/sudoers" /etc/sudoers.d/sopkathon-deploy

cat > /etc/ssh/sshd_config.d/00-sopkathon.conf <<'EOF'
PasswordAuthentication no
KbdInteractiveAuthentication no
PermitRootLogin no
EOF
# Ubuntu includes snippets before the global settings, so place Match at the end.
if ! grep -q '^# SOPKATHON deployment$' /etc/ssh/sshd_config; then
    cat >> /etc/ssh/sshd_config <<'EOF'

# SOPKATHON deployment
Match User sopkathon-deploy
    ForceCommand sudo -n /usr/local/sbin/sopkathon-deploy
    DisableForwarding yes
    PermitTTY no
    PermitUserRC no
EOF
fi
sshd -t
systemctl reload ssh

cat > /etc/systemd/system/sopkathon.service <<'EOF'
[Unit]
Description=SOPKATHON API
After=network-online.target
Wants=network-online.target

[Service]
User=sopkathon-app
Group=sopkathon-app
WorkingDirectory=/opt/sopkathon
Environment=SPRING_PROFILES_ACTIVE=dev
EnvironmentFile=/etc/sopkathon/app.env
ExecStart=/usr/bin/java -Xms96m -Xmx256m -jar /opt/sopkathon/app.jar
SuccessExitStatus=143
Restart=on-failure
RestartSec=3
NoNewPrivileges=yes

[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
systemctl enable sopkathon.service

if [[ ! -f /etc/nginx/sites-available/sopkathon ]]; then
    cat > /etc/nginx/sites-available/sopkathon <<'EOF'
server {
    listen 80 default_server;
    server_name _;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header Forwarded "";
        proxy_set_header X-Forwarded-For $remote_addr;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF
fi
ln -sfn /etc/nginx/sites-available/sopkathon /etc/nginx/sites-enabled/sopkathon
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl enable --now nginx
systemctl reload nginx
