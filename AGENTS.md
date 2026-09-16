# RunMile Agent Guide


## Priority

The prototype must be demo-ready by 2026-09-20. Optimize for an end-to-end competition prototype, not production banking infrastructure.

Default principle:

```text
Simple > Clever
```

## Source of Truth

Before meaningful code changes, read:

1. `README.md`
2. `docs/architecture.md`
3. `docs/api-spec.md`
4. `docs/database.md`
5. `docs/ai-methodology.md`

Do not replace the existing repository structure with a new architecture. If an API or DB schema change is needed, explain the reason and impact before changing it.

## Incremental Development

- Break every feature into the smallest independently verifiable tasks practical.
- Complete and verify one small behavior before starting the next one.
- Keep each task small enough to include implementation, relevant tests, and contract or documentation checks.
- Do not combine multiple unrelated features in one branch or pull request.
- Make broad refactors or prerequisite changes only when they are required for the current Issue.
- Do not move to the next feature while the current behavior remains unverified.
- When a task is too large for one focused pull request, split it into multiple Issues before implementation.

## Communication Language

- 사용자 및 팀원이 읽는 작업 설명, 진행 보고, Issue, PR 제목과 본문, 커밋 요약은 한글로 작성한다.
- 코드 식별자, API 필드, 공통 Enum 값, 라이브러리 및 기술 명칭은 기존 영문 계약을 유지한다.

## GitHub Numbering

- Issue 번호와 PR 번호는 서로 다른 번호다. 예를 들어 Issue `#1`을 처리하는 PR이 `#2`일 수 있다.
- 브랜치명과 커밋 메시지에는 연결된 Issue 번호를 사용하며 PR 번호를 사용하지 않는다.
- PR 제목에는 번호 대신 작업 내용을 작성하고, PR 본문의 `Closes #<issue-number>`로 Issue를 연결한다.

## Fixed Demo Story

Participant:

```text
Daegu Marathon completion
-> NFT VERIFIED
-> +10,000 RunMile
-> merchant search
-> 35,000 KRW payment
   - RunMile 10,000
   - personal payment 25,000
-> transaction saved
```

Admin:

```text
RunMile Impact Dashboard
-> WHERE
-> EFFECT
-> NEXT
```

## Do Not Add

- JWT authentication
- user registration
- full Spring Security setup
- Redis
- Kafka
- microservices
- event sourcing
- CQRS
- Kubernetes
- unnecessary Docker/infrastructure complexity
- separate FastAPI AI server
- new blockchain
- new cryptocurrency/token
- generic success response wrapper
- LLM chatbot/report generator
- complex deep learning

## Integration Honesty

Use `MockDaeguChainAdapter` and `MockDaeguPayAdapter`. Do not claim that real Daegu Chain, Daegu-ro Pay, iM Bank, or financial data integration is complete.
