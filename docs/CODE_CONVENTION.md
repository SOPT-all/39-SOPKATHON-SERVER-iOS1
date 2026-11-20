# 코드 컨벤션

## 목적

이 문서는 서버 코드의 이름, 패키지 경계, 응답과 예외 처리 방식을 통일합니다. 구현 중 예외가 필요하면 해당 PR에서 이유를 설명하고 이 문서를 함께 갱신합니다.

## 네이밍 규칙

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 패키지 | 소문자, 언더스코어 없이 작성 | `domain.store.service` |
| 클래스·Enum·Record | PascalCase | `StoreSearchRequest`, `SortType` |
| 메서드 | camelCase, 동사로 시작 | `findStores`, `createVote` |
| 변수·필드 | camelCase, 명사 사용 | `storeCount`, `closedAt` |
| 상수 | UPPER_SNAKE_CASE | `MAX_RECENT_KEYWORD_COUNT` |
| boolean | `is`, `has` 접두어 사용 | `isClosed`, `hasVoted` |
| DB 테이블·컬럼 | snake_case, 테이블은 단수형 | `store`, `created_at` |
| API URL | kebab-case, 복수 명사, 동사 미사용 | `/api/v1/stores` |
| 쿼리 파라미터 | camelCase | `?ongoingOnly=true&sort=LATEST` |

- 약어 대신 의미가 드러나는 전체 단어를 사용합니다. 예를 들어 `cnt` 대신 `count`를 사용합니다.
- 단건 조회에서 값이 없으면 예외를 던지는 메서드는 `get`, 조건에 맞는 값을 조회하는 메서드는 `find`로 시작합니다.
- 생성·수정·삭제 메서드는 각각 `create`, `update`, `delete`로 시작합니다.
- 계층별 클래스는 `{Domain}Controller`, `{Domain}Service`, `{Domain}Repository` 형식으로 작성합니다.
- DTO는 `{Domain}{Action}Request`, `{Domain}{Action}Response` 형식을 사용하고, 가능하면 `record`로 작성합니다.
- 도메인별 코드 enum은 `{Domain}ErrorCode`, `{Domain}SuccessCode` 형식으로 작성합니다.

## 패키지와 계층 의존

```text
controller → service → repository
```

- 의존은 위 방향으로만 흐릅니다. Controller는 Repository를 직접 호출하지 않습니다.
- 다른 도메인의 데이터가 필요하면 그 도메인의 Repository를 직접 호출하지 않고 Service를 통해 접근합니다.
- 도메인 간 의존은 한 방향으로만 허용하며 순환 의존을 만들지 않습니다.
- Entity는 API 응답으로 직접 반환하지 않고 항상 Response DTO로 변환합니다.
- 공통 코드와 설정은 `common`에, 도메인에만 속한 코드는 `domain/{domain}`에 둡니다.

## API 응답과 예외 처리

- Controller는 `ResponseEntity<CommonResponse<T>>`를 반환하고, 성공 응답은 `CommonResponse.success(...)`로 생성합니다.
- 응답 본문 형식은 `{ status, code, message, data }`로 통일합니다.
- 성공 코드는 `SuccessCode`, 실패 코드는 `ErrorCode`를 구현한 enum으로 관리합니다. 공통 코드는 `common/code`, 도메인 코드는 `domain/{domain}/code`에 둡니다.
- 조회 성공은 HTTP 200, 생성 성공은 HTTP 201을 사용하며, 상태값은 `SuccessCode`가 가집니다.
- 예상 가능한 비즈니스 예외는 `BusinessException(ErrorCode)`로 던지고 `GlobalExceptionHandler`가 한 곳에서 처리합니다.
- 예상하지 못한 예외는 상세 원인을 로그에 남기되, 응답에는 내부 정보를 노출하지 않고 일반적인 500 메시지만 반환합니다.
- 잘못된 요청 값·필수 파라미터 누락·지원하지 않는 HTTP 메서드·검증 실패는 400 또는 405로 응답하며 500으로 처리하지 않습니다.

## 요청 검증과 조회 규칙

- 요청 DTO와 필요한 파라미터에는 `@Valid`와 Bean Validation을 사용합니다.
- 검증 어노테이션에는 사용자에게 보여 줄 한글 `message`를 반드시 지정합니다.
- 목록 응답에는 `page`, `size`, `totalElements`, `totalPages`를 포함합니다.
- 다중 선택 필터는 `?region=서울&region=경기`처럼 같은 파라미터를 반복합니다.

## 리소스와 설정 규칙

- 공통 설정은 `application.yml`, 환경별 설정은 `application-local.yml`, `application-dev.yml`에 둡니다.
- Jackson 시간대는 `spring.jackson.time-zone: Asia/Seoul`로 설정합니다.
- DB 비밀번호·API 키 등 비밀값은 커밋하지 않고 환경 변수로 주입합니다. 공유가 필요한 설정은 `.example` 파일로만 제공합니다.
- 목업 데이터는 `ApplicationRunner` 등 Java 초기화 코드로 넣고, `local`·`dev` 프로필에서만 실행합니다.
- 초기화 코드는 기존 데이터가 있으면 실행하지 않으며, 날짜 데이터는 고정값 대신 현재 시점 기준 상대값을 사용합니다.
