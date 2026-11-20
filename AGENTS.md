# AGENTS.md

39기 PLAY SOPT 솝커톤 iOS 1조 서버. Claude Code·Codex 등 AI 에이전트가 공통으로 따르는 작업 지침입니다.
프로젝트 소개와 Git 규칙은 [README.md](README.md)를 따릅니다.

## 기본 원칙

- AI가 구현하더라도 **왜 그렇게 구현했는지 사람이 설명할 수 있어야** 합니다. 이해하기 어려운 코드는 만들지 않고, 설계 이유가 필요한 부분은 PR에 적습니다.
- 요청받은 범위만 구현합니다. 요청하지 않은 리팩터링, 의존성 추가, 현재 요구사항을 넘는 추상화는 하지 않습니다.
- 없는 클래스나 API를 있는 것처럼 참조하지 않습니다. 코드를 쓰기 전에 실제 파일을 확인합니다.
- 비밀 값(DB 비밀번호, API 키)과 `.env`는 커밋하거나 코드·로그·문서에 적지 않습니다.

## 기술 스택

Java 21 · Spring Boot 4.1.2 · Gradle · Spring Web · Spring Data JPA · Validation · Lombok · H2(local) · Springdoc
(QueryDSL은 필요한 이슈에서 도입)

## 명령어

```bash
./gradlew bootRun    # 로컬 실행 (local 프로필, H2 인메모리)
./gradlew test       # 테스트
./gradlew build      # 빌드
```

Windows PowerShell에서는 `.\gradlew.bat`을 사용합니다. Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 패키지 구조

기본 패키지는 `org.sopt.sopkathon`이고, 도메인형 구조입니다. 계층형·헥사고날 구조는 도입하지 않습니다.

```text
org.sopt.sopkathon
├─ common/            # code, config, dto, entity, exception, util
└─ domain/<도메인>/    # code, controller, service, repository, entity, dto
```

- 의존은 `controller → service → repository` 한 방향입니다. Controller가 Repository를 직접 호출하지 않습니다.
- 다른 도메인의 데이터는 그 도메인의 **Service**를 통해 접근합니다. 도메인 간 의존은 한 방향만 허용하고 순환을 만들지 않습니다.
- Entity는 응답으로 직접 반환하지 않고 항상 Response DTO로 변환합니다.
- 공통 코드는 `common`, 도메인에만 속한 코드는 `domain/<도메인>`에 둡니다.

## 네이밍

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 패키지 | 소문자, 언더스코어 없음 | `domain.job.service` |
| 클래스·Enum·Record | PascalCase | `JobPosting`, `SortType` |
| 메서드 | camelCase, 동사로 시작 | `findJobs`, `scrapJob` |
| 변수·필드 | camelCase, 명사 | `jobs`, `closingDate` |
| 상수 | UPPER_SNAKE_CASE | `MAX_RECENT_KEYWORD_COUNT` |
| boolean | `is`·`has` 접두어 | `isClosed`, `hasWorkTime` |
| DB 테이블·컬럼 | snake_case, 테이블은 단수형 | `job_posting`, `closing_date` |
| API URL | kebab-case, 복수 명사, 동사 금지 | `/api/v1/job-postings` |
| 쿼리 파라미터 | camelCase | `?ongoingOnly=true&sort=HOURLY` |

- 메서드 접두어: 단건 조회 `get`(없으면 예외), 조건 조회 `find`, 생성 `create`, 수정 `update`, 삭제 `delete`.
- 계층 클래스는 `{도메인}Controller`·`{도메인}Service`·`{도메인}Repository`, DTO는 `{도메인}{동작}Request`·`{도메인}{동작}Response`(가능하면 `record`), 코드 enum은 `{도메인}ErrorCode`·`{도메인}SuccessCode`입니다.
- 약어는 풀어서 씁니다. (`cnt` 대신 `count`)

## 공통 응답

- Controller는 항상 `ResponseEntity<CommonResponse<T>>`를 반환하고 `CommonResponse.success(...)`로 만듭니다.
- 응답 본문은 `{ status, code, message, data }`입니다.
- 성공은 `SuccessCode`, 실패는 `ErrorCode`를 구현한 enum으로 관리합니다. 공통은 `common/code`(`GlobalErrorCode`), 도메인별은 `domain/<도메인>/code`에 둡니다. 상태값은 코드 enum이 가집니다. (조회 200, 생성 201)
- 목록 응답에는 `page`, `size`, `totalElements`, `totalPages`를 포함합니다.
- 다중 선택 필터는 `?region=서울&region=경기`처럼 같은 파라미터를 반복합니다.

```java
return CommonResponse.success(JobSuccessCode.JOB_FOUND, JobDetailResponse.of(job));
```

## 예외 처리

- 비즈니스 예외는 `new BusinessException(XxxErrorCode.XXX)`로 던지고, `GlobalExceptionHandler`가 한 곳에서 `CommonResponse.error(...)`로 변환합니다. Controller·Service에서 `try-catch`로 실패 응답을 직접 만들지 않습니다.
- 예상하지 못한 예외는 로그에만 상세를 남기고, 응답은 일반 메시지의 500입니다. 내부 메시지를 노출하지 않습니다.
- 잘못된 파라미터 값·필수 파라미터 누락·지원하지 않는 메서드·검증 실패는 400·405로 응답하고 500으로 떨어뜨리지 않습니다.
- 새 예외 처리가 필요하면 `GlobalExceptionHandler`에 핸들러를 추가합니다.

## 요청 검증

- 요청 DTO와 파라미터는 `@Valid`와 Bean Validation으로 검증하고, 검증 어노테이션에는 **한글 `message`를 반드시 지정**합니다.

## 리소스·설정

- 공통값은 `application.yml`, 환경별 값은 `application-local.yml`·`application-dev.yml`에 둡니다. 시간대는 `spring.jackson.time-zone: Asia/Seoul`입니다.
- 목업 데이터는 `ApplicationRunner`로 넣고(`common/config` 또는 도메인의 `init` 패키지), `local`·`dev` 프로필에서만 실행하며 데이터가 있으면 건너뜁니다. 날짜는 고정값 대신 `LocalDate.now()` 기준 상대값을 씁니다.
- 비밀 값은 환경 변수로 주입하고, 설정 공유가 필요하면 `.example` 파일로만 제공합니다.

## Git 컨벤션

- 브랜치: `feat/#{issue-number}` (영어로 짧게 덧붙일 수 있음). `main`에 직접 push하지 않습니다.
- 커밋: 한글로 `prefix: 내용` — 예: `feat: 회원가입 API 구현`. prefix는 `feat` `fix` `refactor` `chore` `docs` `test` `style`.
- PR: 제목은 `prefix: 내용`(한글), 본문은 [PR 템플릿](.github/pull_request_template.md)을 따르고 `close #이슈번호`로 이슈를 연결합니다. `main` 대상, 리뷰 후 **Merge Commit**으로 머지합니다.
- 작업 흐름: 이슈 생성 → `main`에서 브랜치 분기 → 구현·커밋 → PR → 리뷰·approve → Merge Commit.
