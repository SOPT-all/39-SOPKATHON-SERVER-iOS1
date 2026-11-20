# 39-SOPKATHON-SERVER-iOS1

39기 PLAY SOPT 솝커톤 iOS 1조 서버 레포지토리 🫰💃

<br />

## 👥 팀원 소개

### Backend

| <img src="https://github.com/joobogyeong.png?size=120" width="120" alt="주보경" /> | <img src="https://github.com/eun-seoo.png?size=120" width="120" alt="김은서" /> | <img src="https://github.com/KateteDeveloper.png?size=120" width="120" alt="채윤지" /> |
| :---: | :---: | :---: |
| [주보경](https://github.com/joobogyeong) | [김은서](https://github.com/eun-seoo) | [채윤지](https://github.com/KateteDeveloper) |
| BE 파트장 | BE | BE |

<br />

## 기술 스택

| 구분 | 기술 | 선정 이유 |
| --- | --- | --- |
| Framework | Spring Boot, Spring Web | REST API를 빠르게 구현하기 위해 사용합니다. |
| Persistence | Spring Data JPA | 기본 CRUD와 도메인 모델 영속성을 일관되게 다루기 위해 사용합니다. |
| Query | QueryDSL | 필터·정렬 등 동적 쿼리를 타입 안전하게 조립하기 위해 사용합니다. |
| Validation | Bean Validation | 요청 값 검증을 선언적으로 처리하기 위해 사용합니다. |
| Database | MySQL 8.4 | 로컬 Docker와 배포 환경 RDS에서 같은 MySQL 버전 계열을 사용합니다. |
| Documentation | Springdoc | API 명세를 Swagger UI로 확인하기 위해 사용합니다. |
| Utility | Lombok | 반복되는 보일러플레이트를 줄이기 위해 사용합니다. |

## 구현 범위

- iOS 클라이언트가 사용하는 REST API를 제공합니다.
- 도메인별 비즈니스 로직, 데이터 영속화, 요청 값 검증, 공통 응답과 예외 처리를 서버에서 담당합니다.
- 세부 기능과 API 범위는 이슈와 API 명세를 기준으로 확정합니다.
- 클라이언트 구현, 미확정 외부 연동, 배포 인프라 구현은 별도 이슈에서 다룹니다.

## 패키지 구조

도메인 중심으로 패키지를 구성합니다. 공통 기능은 `common`에, 비즈니스 기능은 `domain` 아래 각 도메인에 둡니다.

```text
{base-package}
├── common
│   ├── code
│   ├── config
│   ├── dto
│   ├── entity
│   ├── exception
│   └── util
└── domain
    └── {domain}
        ├── code
        ├── controller
        ├── service
        ├── repository
        ├── entity
        └── dto
```

상세 코드 규칙은 [코드 컨벤션](docs/CODE_CONVENTION.md)에서 확인할 수 있습니다.

## 로컬 실행

- 팀 공유 기본값은 `.env.example`에 있습니다. 로컬 포트나 비밀번호를 바꾸려면 이 파일을 `.env`로 복사해 수정합니다.
- 서버와 MySQL을 함께 실행하려면 `docker compose up -d --build`를 사용합니다.
- IDE 또는 Java 21에서 서버를 실행하려면 `docker compose up -d --wait mysql` 후 `./gradlew bootRun`을 실행합니다. Windows에서는 `.\gradlew.bat bootRun`을 사용합니다.
- MySQL은 `127.0.0.1:3306`의 `sopkathon` DB를 사용합니다. Docker 포트를 바꾸면 IDE 실행 시 `DB_URL`도 같은 포트로 맞춥니다. `LOCAL_DB_PASSWORD`를 변경했다면 IDE 실행 시 `DB_PASSWORD`도 맞춥니다.
- 서버 상태는 `http://localhost:8080/actuator/health`, API 명세는 `http://localhost:8080/swagger-ui/index.html`에서 확인합니다.

## Git 브랜치 전략

- `main` 브랜치에 직접 push하지 않습니다.
- 모든 작업은 이슈 생성 후 `main`에서 분기한 `feat/#{issue-number}` 브랜치에서 진행합니다.
- 작업 완료 후 `main`을 대상으로 PR을 생성하고, 코드 리뷰를 받은 뒤 **Merge Commit** 방식으로 머지합니다.
- 머지된 feature 브랜치는 필요에 따라 삭제합니다.

### 작업 흐름

1. 이슈를 생성하고 작업 범위를 정리합니다.
2. 최신 `main`에서 `feat/#{issue-number}` 브랜치를 분기합니다.
3. 구현과 검증을 완료한 뒤 커밋합니다.
4. `main`을 대상으로 PR을 생성하고 리뷰를 요청합니다.
5. approve 후 Merge Commit으로 머지합니다.

## 커밋 컨벤션

- 커밋 메시지는 한글로 작성합니다.
- 형식은 `prefix: 커밋 내용`입니다.

| Prefix | 설명 |
| --- | --- |
| `feat:` | 새로운 기능 추가 |
| `fix:` | 버그 수정 |
| `refactor:` | 기능 변경 없는 리팩터링 |
| `chore:` | 빌드·설정 파일 수정 등 |
| `docs:` | 문서 수정 |
| `test:` | 테스트 코드 추가·수정 |
| `style:` | 기능 변경 없는 코드 포맷팅 |

예시: `feat: 회원가입 API 구현`, `fix: 로그인 시 토큰 만료 오류 수정`

## PR 컨벤션

- PR 제목은 커밋 메시지와 같은 `prefix: PR 내용` 형식의 한글로 작성합니다.
- PR 본문에는 관련 이슈를 `close #이슈번호` 형식으로 연결합니다.
- 무엇을, 왜 변경했는지와 리뷰어가 확인할 부분을 작성합니다.
- PR 템플릿의 체크리스트를 완료한 뒤 리뷰를 요청합니다.

## 코드 리뷰 및 머지 규칙

- PR마다 AI 리뷰 또는 팀원 리뷰를 진행하고, 필요한 내용은 코멘트로 남깁니다.
- approve 횟수에는 제한을 두지 않습니다.
- 리뷰를 마친 PR만 `main`에 Merge Commit 방식으로 머지합니다.
