#!/usr/bin/env bash
set -euo pipefail
umask 077
endpoint=${1:?RDS endpoint required}
[[ "$endpoint" =~ ^[a-z0-9.-]+\.rds\.amazonaws\.com$ ]]

exec 9>/run/lock/sopkathon-init-db.lock
flock -x 9
if [[ -s /etc/sopkathon/app.env ]]; then
    exit 0
fi

tmp_dir=$(mktemp -d /etc/sopkathon/.init-db.XXXXXXXX)
trap 'rm -rf -- "$tmp_dir"' EXIT
# Read the RDS-managed secret from the SSH stream without writing it to console output.
python3 -c '
import json, sys

secret = json.load(sys.stdin)
password = secret["password"].replace("\\", "\\\\").replace(chr(34), "\\" + chr(34))
print("[client]")
print("user=" + secret["username"])
print("password=" + chr(34) + password + chr(34))
' > "$tmp_dir/mysql.cnf"

password=$(openssl rand -hex 24)
mysql --defaults-extra-file="$tmp_dir/mysql.cnf" --host="$endpoint" \
    --ssl-mode=VERIFY_IDENTITY --ssl-ca=/etc/sopkathon/rds-ca-bundle.pem <<SQL
CREATE USER IF NOT EXISTS 'sopkathon_app'@'%' IDENTIFIED BY '${password}' REQUIRE SSL;
ALTER USER 'sopkathon_app'@'%' IDENTIFIED BY '${password}' REQUIRE SSL;
GRANT SELECT, INSERT, UPDATE, DELETE ON sopkathon.* TO 'sopkathon_app'@'%';
SQL

{
    printf 'DB_URL=jdbc:mysql://%s:3306/sopkathon\n' "$endpoint"
    printf 'DB_USERNAME=sopkathon_app\nDB_PASSWORD=%s\n' "$password"
} > "$tmp_dir/app.env"
mv "$tmp_dir/app.env" /etc/sopkathon/app.env
