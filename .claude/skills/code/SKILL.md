---
name: code
description: "기획자가 컨펌한 스펙을 대시보드에서 읽어 GitHub 이슈와 feat/#N 브랜치를 만들고, AGENTS.md 컨벤션에 맞게 구현·테스트·커밋한다. 사용자가 /code 스펙ID 라고 할 때 사용."
argument-hint: "스펙 ID (예: login-api). 이슈가 이미 있는 기존 스펙은 이슈 번호"
---

# /code — 이슈 생성 + 스펙 기반 구현 (2단계)

컨펌된 스펙(`$ARGUMENTS`)으로 **GitHub 이슈를 만들고**, 구현하고, **PR 초안까지 써 둔다**. ID가 없으면 한 번만 묻는다. 필요한 만큼만 구현한다: 스펙을 만족하는 가장 단순한 코드가 목표이고, 나중에 `/review`가 오버엔지니어링을 점검한다.

스펙은 대시보드 레포의 `specs` 브랜치에, 기획자 승인은 대시보드(Firestore)에 있다. `docs/ai/dashboard.json`의 `dashboardRepo`를 `<R>`, `firebaseProjectId`를 `<P>`라 하자. 승인은 공개 읽기라 로그인이 필요 없고, 값은 `fields.<이름>.stringValue`에 들어 있다.

```bash
gh api "repos/<R>/contents/specs/<id>.md?ref=specs" -H "Accept: application/vnd.github.raw+json"
curl -s "https://firestore.googleapis.com/v1/projects/<P>/databases/(default)/documents/approvals/<id>"
```

## 1. 게이트 (실패하면 중단하고 이유만 알린다)

- **스펙 존재**: `specs/<id>.md`가 404면 중단한다: "대시보드에 올라간 스펙이 아닙니다. `/spec`으로 먼저 만들어 올리세요." 본문은 파일 내용 전체, 제목은 첫 줄 `# 제목`이다.
- **기획자 컨펌**: `approvals/<id>`의 `fields.status.stringValue`가 `approved`면 통과다(`fields.by`, `fields.comment`는 보고 시 인용). 문서가 없거나(404) `changes`면 중단한다: "기획자 컨펌 전입니다. 대시보드에서 승인을 받으세요 (`dashboardUrl`)." `changes`면 코멘트를 보여 주고 `/spec <id>`로 개정하라고 안내한다.
- **승인 이후 개정 여부**: `approvals`의 `fields.specHash`는 승인 당시 스펙 본문의 SHA-256(소문자 hex, UTF-8)이다. 스펙 파일 내용을 임시 파일에 그대로 저장해 `node -e "process.stdout.write(require('crypto').createHash('sha256').update(require('fs').readFileSync(0,'utf8')).digest('hex'))" < 파일`로 계산해 비교한다. 다르면 승인 이후 스펙이 바뀐 것이니 중단하고 재승인을 안내한다.
- **기존 이슈**: 이 스펙의 이슈가 이미 있는지 `gh issue list --label spec --state all --search "sopkathon:spec:<id> in:body" --json number,state`로 확인한다. 열린 이슈가 있으면 새로 만들지 않고 그 번호를 `N`으로 쓴다(이어서 작업). 닫힌 이슈만 있으면 중단하고 알린다.
- **이슈가 이미 있는 기존 스펙**: 인자가 숫자(이슈 번호)면 대시보드 스펙이 없는 예전 방식 스펙이다. 이슈 본문이 스펙이고, 승인 문서는 `approvals/issue-<번호>`다. 이슈는 새로 만들지 않고 번호를 `N`으로 쓴다.
- 워킹 트리가 깨끗한지 `git status --porcelain`으로 확인한다. 변경이 있으면 사용자에게 알리고 멈춘다.
- `gh auth status`가 실패하면 `gh auth login`을 안내한다.

## 2. 이슈 생성과 준비

1. `AGENTS.md`와 `docs/CODE_CONVENTION.md`(있으면)를 읽는다. 둘이 어긋나면 `docs/CODE_CONVENTION.md`를 우선하고 사용자에게 알린다. **패키지 구조, 네이밍, 공통 응답, 예외 처리, 검증, 설정 규칙을 그대로 따른다.** 코드를 쓰기 전에 관련 기존 파일(Controller, `CommonResponse`, `BusinessException`, 에러 코드 enum 등)을 실제로 열어 본다. 없는 클래스나 API를 있는 것처럼 쓰지 않는다.
2. 스펙의 **요구사항(FR)과 완료 조건**을 체크리스트로 정리해 사용자에게 한 번 보여 주고, 아래 이슈를 만들어도 되는지 확인한다. 범위를 넘는 일(리팩터링, 의존성 추가)은 계획에 넣지 않는다.
3. **이슈 생성** (기존 이슈가 없을 때만). 라벨이 없으면 만든다(이미 있으면 무시): `gh label create spec --color 5319e7 -d "기능 스펙"`, `stage:coding`(1d76db), `stage:review`(c5def5), `stage:pr`(bfdadc), `stage:changes`(d93f0b). 본문은 스펙 본문 + 맨 아래 한 줄 `<!-- sopkathon:spec:<id> -->`(대시보드가 스펙과 이슈를 잇는 표식, **반드시 그대로**)로 임시 파일에 만들고:
   ```bash
   gh issue create --title "<스펙 제목>" --body-file <파일> --label spec --label stage:coding --assignee @me
   ```
   출력된 URL의 번호가 `N`이다. 임시 파일은 끝나면 지운다.
4. 브랜치를 만든다: `git switch main && git pull --ff-only && git switch -c feat/#N`. 이미 있으면 그 브랜치로 전환한다. `main`에는 직접 커밋하지 않는다.
5. 기존 이슈를 이어받은 경우 라벨을 옮긴다: `gh issue edit N --remove-label stage:spec --remove-label stage:approved --add-label stage:coding` (없는 라벨 제거 오류는 무시). 이어서(이슈를 새로 만든 경우도 포함) `docs/ai/dashboard.json`의 `dashboardRepo`로 `gh workflow run deploy.yml -R <dashboardRepo>`를 실행해 대시보드를 갱신한다(실패해도 무시하고 계속, `dashboardRepo`가 비어 있으면 건너뜀).

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

구현한 FR 목록, 테스트 결과, 스펙과 달라진 점이나 `[확인 필요]`로 남은 것을 요약하고 `/review <id>`로 넘어가라고 안내한다. 이슈 번호 `N`과 브랜치 이름도 함께 알린다. 스펙과 다르게 구현해야 했다면 이슈에 `gh issue comment N`으로 이유를 남긴다.

## 규칙

- 요청받은 범위만 구현한다. 비밀 값·`.env`는 코드, 로그, 문서에 적지 않는다.
- 스펙에 없는 요구사항을 임의로 추가하지 않는다. 필요해 보이면 이슈 코멘트로 기획자에게 묻는다.
