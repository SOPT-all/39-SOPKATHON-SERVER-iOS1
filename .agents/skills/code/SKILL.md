---
name: code
description: "기획자가 컨펌한 스펙 이슈를 읽고 AGENTS.md 컨벤션에 맞게 구현·테스트·커밋한다. 사용자가 /code 이슈번호 라고 할 때 사용."
argument-hint: "스펙 이슈 번호 (예: 12)"
---

# /code — 스펙 기반 구현 (2단계)

컨펌된 스펙 이슈(`$ARGUMENTS`)를 구현하고 **PR 초안까지 써 둔다**. 번호가 없으면 한 번만 묻는다. 필요한 만큼만 구현한다: 스펙을 만족하는 가장 단순한 코드가 목표이고, 나중에 `/review`가 오버엔지니어링을 점검한다.

## 1. 게이트 (실패하면 중단하고 이유만 알린다)

```bash
gh issue view N --json number,title,body,labels,state
```

- **기획자 컨펌 확인.** 컨펌은 대시보드(Firestore)에 저장된다. `docs/ai/dashboard.json`의 `firebaseProjectId`로 읽는다.
  ```bash
  curl -s "https://firestore.googleapis.com/v1/projects/<firebaseProjectId>/databases/(default)/documents/approvals/N"
  ```
  응답의 `fields.status.stringValue`가 `approved`면 통과다 (`fields.by`, `fields.comment`도 보고 시 인용). 문서가 없거나(404) `changes`면 중단한다: "기획자 컨펌 전입니다. 대시보드에서 승인을 받으세요 (`dashboardUrl`)." `changes`면 코멘트를 보여 주고 `/spec N`으로 개정하라고 안내한다. `firebaseProjectId`가 비어 있으면 이슈에 `stage:approved` 라벨이 있는지로 대신 확인한다.
- 스펙이 승인 뒤에 개정되었을 수 있다(대시보드가 "승인 이후 스펙이 바뀜"으로 표시). 이슈 최종 수정 시각이 승인 시각(`fields.at.timestampValue`)보다 늦고 본문이 바뀐 듯하면 사용자에게 알리고 재승인 여부를 묻는다.
- 이슈가 닫혀 있으면 중단한다.
- 워킹 트리가 깨끗한지 `git status --porcelain`으로 확인한다. 변경이 있으면 사용자에게 알리고 멈춘다.

## 2. 준비

1. `AGENTS.md`와 `docs/CODE_CONVENTION.md`(있으면)를 읽는다. 둘이 어긋나면 `docs/CODE_CONVENTION.md`를 우선하고 사용자에게 알린다. **패키지 구조, 네이밍, 공통 응답, 예외 처리, 검증, 설정 규칙을 그대로 따른다.** 코드를 쓰기 전에 관련 기존 파일(Controller, `CommonResponse`, `BusinessException`, 에러 코드 enum 등)을 실제로 열어 본다. 없는 클래스나 API를 있는 것처럼 쓰지 않는다.
2. 스펙의 **요구사항(FR)과 완료 조건**을 체크리스트로 정리해 사용자에게 한 번 보여 준다. 범위를 넘는 일(리팩터링, 의존성 추가)은 계획에 넣지 않는다.
3. 브랜치를 만든다: `git switch main && git pull --ff-only && git switch -c feat/#N`. 이미 있으면 그 브랜치로 전환한다. `main`에는 직접 커밋하지 않는다.
4. 라벨을 옮긴다: `gh issue edit N --remove-label stage:spec --remove-label stage:approved --add-label stage:coding` (없는 라벨 제거 오류는 무시). 이어서 `docs/ai/dashboard.json`의 `dashboardRepo`로 `gh workflow run deploy.yml -R <dashboardRepo>`를 실행해 대시보드를 갱신한다(실패해도 무시하고 계속, `dashboardRepo`가 비어 있으면 건너뜀).

## 3. 구현

- 의존 방향은 `controller → service → repository`. Entity는 응답으로 직접 반환하지 않고 Response DTO로 변환한다.
- 요청 DTO에는 `@Valid`와 **한글 `message`가 있는** Bean Validation을 쓴다.
- 실패는 `BusinessException`과 도메인 `ErrorCode`로 던지고 `GlobalExceptionHandler`에 맡긴다. `try-catch`로 실패 응답을 직접 만들지 않는다.
- 스펙 요구사항마다 대응하는 테스트를 추가한다 (Service 단위 테스트, 필요하면 Controller 슬라이스 테스트).
- 설계 이유가 필요한 부분은 나중에 PR에 적을 수 있도록 메모해 둔다. 사람이 설명할 수 없는 코드는 만들지 않는다.

## 4. 검증

```bash
./gradlew test      # Windows PowerShell: .\gradlew.bat test
./gradlew build
```

실패하면 원인을 고치고 다시 돌린다. 끝내 못 고치면 실패 출력을 그대로 보고하고 멈춘다. 통과한 것처럼 말하지 않는다.

## 5. 커밋

한글 `prefix: 내용` 형식으로, 논리 단위로 나눠 커밋한다 (`feat` `fix` `refactor` `chore` `docs` `test` `style`). 예: `feat: 투표 생성 API 구현`. 푸시와 PR은 하지 않는다 (`/pr` 담당).

## 6. PR 초안 작성 (구현을 가장 잘 아는 지금 쓴다)

구현이 끝나면 PR 본문 초안을 이슈 코멘트로 남긴다. 파일은 만들지 않는다. `.github/pull_request_template.md`가 있으면 그 구조를, 없으면 `## 작업 내용`, `## 설계 이유`, `## 테스트`, `## 리뷰 포인트`를 쓴다.

- **작업 내용**: 스펙 FR 기준으로 무엇을 구현했는지.
- **설계 이유**: 비자명한 결정과 그렇게 한 이유. 사람이 설명할 수 있어야 하므로 3단계에서 메모한 것을 여기에 옮긴다.
- **테스트**: 실행한 명령과 결과(통과/실패 그대로).
- **리뷰 포인트**: 리뷰어가 먼저 볼 곳, 스펙과 달라진 점, `[확인 필요]`.
- 맨 아래에 `close #N`.

첫 줄에 마커를 **반드시 그대로** 둔다. 이미 초안 코멘트가 있으면 새로 달지 않고 수정한다(`gh issue comment N --edit-last` 또는 해당 코멘트 갱신).

```markdown
<!-- sopkathon:pr-draft -->
제목: feat: 투표 생성 API 구현

## 작업 내용
…
```

제목은 한글 `prefix: 내용`. 이슈 라벨은 이미 `stage:coding`이다.

## 7. 보고

구현한 FR 목록, 테스트 결과, 스펙과 달라진 점이나 `[확인 필요]`로 남은 것을 요약하고 `/review N`으로 넘어가라고 안내한다. 스펙과 다르게 구현해야 했다면 이슈에 `gh issue comment N`으로 이유를 남긴다.

## 규칙

- 요청받은 범위만 구현한다. 비밀 값·`.env`는 코드, 로그, 문서에 적지 않는다.
- 스펙에 없는 요구사항을 임의로 추가하지 않는다. 필요해 보이면 이슈 코멘트로 기획자에게 묻는다.
