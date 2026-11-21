#!/usr/bin/env bash
set -euo pipefail

cd /opt/sopkathon
exec 9>/run/lock/sopkathon-deploy.lock
flock -x 9

candidate=$(mktemp --tmpdir=. .candidate.XXXXXXXX)
trap 'rm -f -- "$candidate"' EXIT
cat > "$candidate"
test -s "$candidate"
chmod 0644 "$candidate"

if [[ -f app.jar ]]; then
    test ! -e previous.jar.tmp || rm -f previous.jar.tmp
    ln app.jar previous.jar.tmp
    mv -f previous.jar.tmp previous.jar
fi
mv -f -- "$candidate" app.jar

if systemctl restart sopkathon.service; then
    for attempt in {1..30}; do
        if curl -fsS --max-time 2 http://127.0.0.1:8080/actuator/health >/dev/null 2>&1; then
            exit 0
        fi
        sleep 3
    done
fi

printf '%s\n' '배포 헬스 체크 실패. 이전 버전으로 복구합니다.' >&2
if [[ -f previous.jar ]]; then
    mv -f previous.jar app.jar
    systemctl restart sopkathon.service
else
    systemctl stop sopkathon.service
    rm -f app.jar
fi
exit 1
