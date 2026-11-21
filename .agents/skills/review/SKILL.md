---
name: review
description: "/code로 구현한 브랜치가 스펙에 맞는지, 코드 컨벤션을 지키는지, 오버엔지니어링은 아닌지 점검하고 결과를 이슈에 남긴다. 사용자가 /review 스펙ID 라고 할 때 사용."
argument-hint: "스펙 ID 또는 이슈 번호 (생략하면 현재 브랜치 feat/#N에서 추정)"
---

# /review — 구현 점검: 맞는가·컨벤션·오버엔지니어링 (3단계)

현재 브랜치의 변경(`main` 대비)을 점검한다. 이슈 번호 `N`은 브랜치명 `feat/#N`에서 얻는다. `$ARGUMENTS`가 스펙 ID면 `gh issue list --label spec --state all --search "sopkathon:spec:<id> in:body" --json number`로 `N`을 찾는다(숫자면 그대로 `N`). 찾지 못하면 한 번만 묻는다.

## 절차

1. **자료 수집**
   ```bash
   gh issue view N --json title,body,labels
   git diff main...HEAD --stat
   git diff main...HEAD
   ```
   `AGENTS.md`와 `docs/CODE_CONVENTION.md`(있으면)를 읽는다. 컨벤션의 단일 출처다. 여기서 규칙을 새로 만들지 않는다. 둘이 어긋나면 `docs/CODE_CONVENTION.md`를 우선하고 어긋남 자체를 권장 항목으로 적는다.
2. **빌드·테스트**: `./gradlew test`를 돌려 결과를 기록한다. 실패하면 첫 항목으로 올린다.
3. **체크리스트** — 각 항목을 `통과 / 위반 / 해당 없음`으로 판정하고, 위반은 `파일:줄`과 이유를 적는다.
   - **구조**: 패키지가 `domain/<도메인>/{controller,service,repository,entity,dto,code}`를 따르는가. Controller가 Repository를 직접 부르지 않는가. 다른 도메인은 Service를 통해 접근하는가. 순환 의존이 없는가.
   - **네이밍**: 클래스 PascalCase, 메서드 동사 시작(`get/find/create/update/delete`), boolean `is/has`, 테이블·컬럼 snake_case 단수, URL kebab-case 복수 명사·동사 금지, 쿼리 파라미터 camelCase, 약어 미사용.
   - **응답**: Controller가 `ResponseEntity<CommonResponse<T>>`를 반환하고 `CommonResponse.success(...)`를 쓰는가. 조회 200, 생성 201. Entity를 직접 반환하지 않는가. 목록 응답에 `page, size, totalElements, totalPages`가 있는가.
   - **예외**: `BusinessException` + `ErrorCode` enum을 쓰는가. Controller·Service의 `try-catch`로 실패 응답을 만들지 않는가. 내부 메시지를 응답에 노출하지 않는가.
   - **검증**: 요청 DTO에 `@Valid`와 한글 `message`가 있는가.
   - **설정·보안**: 비밀 값, `.env`, 키가 코드·로그·문서에 없는가. 설정이 `application*.yml` 규칙을 따르는가.
   - **범위**: 요청하지 않은 리팩터링, 의존성 추가, 과한 추상화가 없는가. 없는 클래스를 참조하지 않는가.
   - **스펙 충족**: 이슈의 요구사항(FR)과 완료 조건이 각각 구현·테스트되었는가. 빠진 것과 스펙에 없는 것을 구분해 적는다.
   - **커밋**: 메시지가 한글 `prefix: 내용` 형식인가.
   - **오버엔지니어링** — `/code`가 스펙보다 많이 만들지 않았는지 본다. 각 항목에 근거(파일·줄)를 단다.
     - 스펙 FR·완료 조건에 대응하지 않는 코드·API·필드·옵션이 있는가 (요청 밖 기능, 미리 만든 확장 포인트).
     - 구현체가 하나뿐인 인터페이스, 호출처가 하나뿐인 헬퍼·유틸·추상 클래스, 불필요한 계층·래퍼·빌더가 있는가.
     - 쓰이지 않는 코드(메서드, 필드, enum 값, import, DTO)가 있는가.
     - 더 단순하게 같은 일을 하는 방법이 있는가 (기존 공통 코드 재사용 가능, 한 줄로 되는 것을 클래스로 만듦).
     - 새 의존성이나 설정이 추가되었는가, 추가됐다면 스펙이 필요로 했는가.
     - 스펙에 없는 테스트 대상(과한 목 설정, 구현 세부 검증)이 있는가.
     과하다고 판단하면 **무엇을 어떻게 줄일지**를 함께 적는다. 단순하고 스펙에 딱 맞으면 "적정"이라고 판정한다.
4. **결과 정리**: 심각도를 `필수 수정`(컨벤션 위반, 스펙 누락, 버그, 실패하는 테스트) / `권장`(가독성, 중복)으로 나눈다. 확신이 낮은 지적은 "의심"이라고 표시한다. 칭찬이나 군더더기는 쓰지 않는다.
5. **이슈에 기록**: 아래 형식으로 `gh issue comment N --body-file <파일>`. 첫 줄의 마커는 대시보드가 읽으므로 **반드시 그대로** 둔다.
   ```markdown
   <!-- sopkathon:review -->
   ## 코드 리뷰 결과: 통과 | 수정 필요
   - 빌드·테스트: 통과/실패
   - 구현 판정: 스펙에 맞음 / 일부 누락 · 규모: 적정 / 오버엔지니어링
   - 필수 수정 N건 · 권장 M건

   ### 필수 수정
   1. `경로/File.java:42` — 이유
   ### 권장
   - …
   ### 스펙 완료 조건
   - [x] FR-1 …
   - [ ] FR-2 … (미구현)
   ```
   라벨: `gh issue edit N --remove-label stage:coding --add-label stage:review`. 이어서 `docs/ai/dashboard.json`의 `dashboardRepo`로 `gh workflow run deploy.yml -R <dashboardRepo>`를 실행해 대시보드를 갱신한다(실패해도 무시하고 계속, `dashboardRepo`가 비어 있으면 건너뜀).
6. **수정 제안**: 필수 수정이 있으면 사용자에게 고칠지 묻는다. 동의하면 고치고(오버엔지니어링은 코드를 **줄이는** 방향으로) 테스트를 다시 돌려 새 커밋으로 남긴 뒤 리뷰 코멘트를 갱신한다. 코드가 달라졌으면 `<!-- sopkathon:pr-draft -->` 초안 코멘트도 맞게 고친다. 임의로 고치지 않는다. 필수 수정이 없고 규모가 적정이면 `/pr N`으로 안내한다.

## 규칙

- 이 단계는 **읽기와 보고**가 기본이다. 사용자 동의 없이 코드를 수정하지 않는다.
- 판정은 근거(파일·줄·AGENTS.md 항목)가 있어야 한다. 취향 차이를 위반으로 올리지 않는다.
