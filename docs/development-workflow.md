# RunMile Development Workflow

## 1. Purpose

This document defines the collaboration rules for the RunMile prototype.

The team has a short development window, so the goal is to keep GitHub Issues, branches, commits, and PRs easy to trace.

## 2. Basic Rule

Create one GitHub Issue for each clear task.

One branch should usually work on one issue.

One PR should usually close one issue.

## 3. Branch Naming

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

## 4. Type

Use one of:

```text
feat      new feature
fix       bug fix
docs      documentation only
refactor  code restructuring without behavior change
chore     setup, dependency, config, cleanup
test      test code
```

## 5. Area

Use one of:

```text
fe       frontend
be       backend
ai       AI/data
db       database/schema/seed
docs     documentation
common   cross-area or project-wide work
```

## 6. Commit Message

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

## 7. Pull Request

PR title should mirror the issue/task:

```text
feat(be): implement RunMile issue API
```

PR description should include:

```text
Closes #1
```

Also include:

- What changed
- How it was tested
- Any known limitation

## 8. Working With Docs

Before implementing, check:

```text
README.md
docs/architecture.md
docs/api-spec.md
docs/database.md
docs/ai-methodology.md
```

Do not silently change API or DB contracts. If a contract change is needed, update the relevant docs in the same PR.

## 9. Prototype Scope

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
