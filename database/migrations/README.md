# Database Migration Rules

RunMile은 짧은 개발 기간 동안 별도 migration runner를 도입하지 않고 PostgreSQL SQL을
수동 적용한다. 기존 Docker 볼륨을 사용하는 팀원은 새 migration을 번호 순서대로 실행한다.

## Naming

```text
NNN_short_description.sql
```

- 번호는 세 자리 오름차순으로 사용한다.
- 이미 공유되거나 적용된 migration 파일은 수정하지 않는다.
- 다음 변경은 새 번호의 파일로 추가한다.
- 가능한 경우 `IF NOT EXISTS` 등 재실행 가능한 SQL을 사용한다.
- 스키마 변경 시 `database/init.sql`과 `docs/database.md`도 함께 수정한다.

## Apply

```bash
docker compose exec -T postgres psql -U runmile -d runmile \
  < database/migrations/001_ai_evaluation.sql
```

새 migration을 추가한 사람은 적용 명령과 순서를 PR 본문에 기록한다.
