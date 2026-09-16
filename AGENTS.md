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
