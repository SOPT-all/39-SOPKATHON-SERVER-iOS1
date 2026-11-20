# 솝커톤 서버 접속과 배포

## 팀 접속

1. 각자 `ssh-keygen -t ed25519 -f "$HOME/.ssh/sopkathon-ios1"`로 키를 만듭니다. 출력 파일 중 `sopkathon-ios1.pub` 내용과 행사장에서 접속할 공인 IPv4를 인프라 담당자에게 전달합니다. Windows에서는 `curl.exe https://checkip.amazonaws.com`, macOS와 Linux에서는 `curl https://checkip.amazonaws.com`으로 IPv4를 확인합니다.
2. SSH 키가 등록되면 `ssh -i "$HOME/.ssh/sopkathon-ios1" <계정>@<EC2_EIP>`로 접속합니다. 처음 접속할 때는 인프라 담당자가 전달한 SSH 호스트 키 지문을 확인합니다.
3. DB가 필요하면 MySQL Workbench의 **Standard TCP/IP over SSH**로 접속합니다. SSH 호스트는 EC2 Elastic IP, DB 호스트는 RDS 엔드포인트, DB 포트는 `3306`, 스키마는 `sopkathon`입니다. 각자 발급된 DB 계정을 사용합니다. RDS에서 제공하는 서울 리전 CA 번들로 DB TLS 인증서를 검증합니다.

## 배포 운영

- AWS 서울 리전의 `sopkathon-ios1-dev` CloudFormation 스택에서 EC2, Elastic IP, RDS 및 배포 역할의 값을 확인합니다. EC2 콘솔에서 인스턴스를 선택하고 **연결 → Session Manager**로도 접속할 수 있습니다.
- GitHub Actions는 PR에서 테스트하고, `main`에 반영되면 Java 21 JAR를 구워 EC2에 전송합니다. 배포에 필요한 저장소 변수는 `AWS_DEPLOY_ROLE_ARN`, `EC2_INSTANCE_ID`, `EC2_SSH_HOST_PUBLIC_KEY`입니다. 호스트 공개 키는 EC2의 `/etc/ssh/ssh_host_ed25519_key.pub`에서 `ssh-ed25519`와 키 값 두 필드만 사용합니다.
- 운영 로그는 EC2에서 `sudo journalctl -u sopkathon -n 100`으로 확인합니다. `curl -fsS http://127.0.0.1:8080/actuator/health`로 앱과 DB 상태를 확인합니다.
- `dev`에서 JPA는 스키마를 검증합니다. 엔티티 변경이 들어가면 DB 관리자나 지정 배포 담당자가 호환되는 스키마 변경을 먼저 적용합니다. JAR 롤백은 DB 스키마를 되돌리지 않습니다.
- AWS 콘솔 비밀번호, SSH 개인 키, 운영 DB 비밀번호는 채팅이나 저장소에 올리지 않습니다. RDS 관리자 자격 증명은 AWS Secrets Manager에, API 서버의 DB 환경 변수는 EC2의 `/etc/sopkathon/app.env`에 있습니다.

## HTTPS

프로젝트명이 정해지면 내도메인.한국에서 선택한 호스트의 A 레코드를 EC2 Elastic IP로 지정합니다. DNS가 반영되면 Nginx 사이트에 호스트 이름을 설정하고 Certbot으로 HTTPS와 HTTP 리다이렉트를 적용합니다. `sudo certbot renew --dry-run`과 iOS 기기의 실제 API 호출로 완료를 확인합니다.
