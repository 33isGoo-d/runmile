# RunMile Development Workflow

## 1. Purpose

This document defines the collaboration rules for the RunMile prototype.

The team has a short development window, so the goal is to keep GitHub Issues, branches, commits, and PRs easy to trace.

## 2. Basic Rule

Create one GitHub Issue for each clear task.

One branch should usually work on one issue.

One PR should usually close one issue.

## 3. Small-Batch Development

RunMile is developed by completing small, independently verifiable tasks in sequence.

1. Split each feature into the smallest unit that produces one clear result.
2. Give each Issue one outcome and an explicit completion condition.
3. Use one branch for one Issue.
4. Keep each PR focused on one feature or one change purpose.
5. Complete the relevant build and tests before starting the next task.
6. For shared contracts such as REST APIs and database schemas, update the documentation first and share the change with the team.

As a practical guideline, an Issue should usually be small enough to finish within half a day. Split an Issue when it combines multiple areas such as:

- Database schema changes
- Backend API implementation
- Frontend screen integration
- AI batch or analysis logic

For example, do not implement the entire RunMile issuance feature as one task. Split it into focused Issues such as:

1. Confirm the issuance API request and response contract.
2. Implement the wallet JPA entity and repository.
3. Implement completion eligibility validation.
4. Connect the NFT verification Mock Adapter.
5. Save the issuance transaction.
6. Prevent duplicate issuance.
7. Add issuance API integration tests.
8. Connect the frontend issuance action.

## 4. Branch Naming

Use this short branch naming format:

```text
<type>/<area>/<issue-number>-<short-title>
```

Examples:

```text
feat/be/1-wallet
feat/fe/3-participant-flow
feat/ai/5-synthetic-generator
fix/be/7-payment-validation
docs/common/8-update-api-spec
```

Do not include `#` in branch names.

Good:

```text
feat/be/1-wallet
```

Avoid:

```text
feat/be/#1-wallet
```

## 5. Type

Use one of:

```text
feat      new feature
fix       bug fix
docs      documentation only
refactor  code restructuring without behavior change
chore     setup, dependency, config, cleanup
test      test code
```

## 6. Area

Use one of:

```text
fe       frontend
be       backend
ai       AI/data
db       database/schema/seed
docs     documentation
common   cross-area or project-wide work
```

## 7. Commit Message

Use this format:

```text
<type>(<area>): <short summary> (#<issue-number>)
```

Examples:

```text
feat(be): implement RunMile issue API (#1)
feat(fe): build participant payment flow (#3)
feat(ai): generate synthetic merchant sales data (#5)
fix(be): validate payment RunMile balance (#7)
docs(common): update API and DB contracts (#8)
```

Commits do not need to be perfect, but they should make it clear which issue they belong to.

## 8. Pull Request

PR title should mirror the issue/task:

```text
feat(be): implement RunMile issue API
```

PR description should include:

```text
Closes #1
```

Issue 번호와 PR 번호는 서로 별개다. GitHub 저장소의 생성 순서에 따라 Issue `#1` 다음에 만든 PR은 `#2`가 될 수 있다.

```text
연결된 Issue: #1
생성된 PR:    #2
브랜치 번호:  1  (Issue 번호 사용)
커밋의 번호: #1  (Issue 번호 사용)
```

PR 제목에는 PR 번호나 Issue 번호만 적지 않고 작업 내용을 적는다. 연결할 Issue는 PR 본문의 `Closes #1`로 명시한다.

Also include:

- What changed
- How it was tested
- Any known limitation

## 9. Working With Docs

Before implementing, check:

```text
README.md
docs/architecture.md
docs/api-spec.md
docs/database.md
docs/ai-methodology.md
```

Do not silently change API or DB contracts. If a contract change is needed, update the relevant docs in the same PR.

## 10. Prototype Scope

Do not add infrastructure that is outside the prototype scope unless the team agrees first.

Avoid:

- JWT authentication
- Spring Security setup
- Redis
- Kafka
- microservices
- Kubernetes
- separate FastAPI AI server
- new blockchain or token
- participant AI chatbot

Follow:

```text
Simple > Clever
Demo > Infrastructure
Working End-to-End Flow > Feature Count
```
