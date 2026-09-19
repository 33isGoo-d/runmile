#!/usr/bin/env bash

set -euo pipefail

if [[ "${RUNMILE_CONFIRM_DEMO_RESET:-}" != "RESET" ]]; then
    echo "실행을 확인하려면 RUNMILE_CONFIRM_DEMO_RESET=RESET을 지정하세요." >&2
    exit 1
fi

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
database_name="${POSTGRES_DB:-runmile}"
database_user="${POSTGRES_USER:-runmile}"

cd "${repo_root}"
docker compose exec -T postgres \
    psql -v ON_ERROR_STOP=1 -U "${database_user}" -d "${database_name}" \
    < database/reset-demo.sql

echo "로컬 데모 지급 및 결제 상태를 초기화했습니다. 온체인 완주 증명은 유지됩니다."
