from __future__ import annotations

from datetime import datetime

import pandas as pd
import psycopg

from common.config import (
    POSTGRES_DB,
    POSTGRES_HOST,
    POSTGRES_PASSWORD,
    POSTGRES_PORT,
    POSTGRES_USER,
    PROCESSED_DIR,
    RESULTS_DIR,
    SYNTHETIC_DIR,
)


def _connect() -> psycopg.Connection:
    return psycopg.connect(
        host=POSTGRES_HOST,
        port=POSTGRES_PORT,
        dbname=POSTGRES_DB,
        user=POSTGRES_USER,
        password=POSTGRES_PASSWORD,
    )


def _merchant_rows(merchants: pd.DataFrame) -> list[tuple]:
    rows = []
    for merchant in merchants.itertuples(index=False):
        rows.append(
            (
                f"AI_{merchant.merchant_code}",
                f"합성 {merchant.district} {merchant.category} {merchant.merchant_id}",
                merchant.district,
                merchant.category,
                f"대구광역시 {merchant.district}",
                bool(merchant.runmile_enabled),
            )
        )
    return rows


def _sales_rows(sales: pd.DataFrame, merchant_ids: dict[int, int]) -> list[tuple]:
    return [
        (
            merchant_ids[int(row.merchant_id)],
            row.date,
            int(row.sales_amount),
            int(row.transaction_count),
            float(row.temperature),
            float(row.rainfall),
            bool(row.is_weekend),
            bool(row.is_marathon_day),
            row.scenario,
        )
        for row in sales.itertuples(index=False)
    ]


def _prediction_rows(
    predictions: pd.DataFrame,
    merchant_ids: dict[int, int],
) -> list[tuple]:
    return [
        (
            merchant_ids[int(row.merchant_id)],
            row.date,
            row.scenario,
            int(row.actual_sales),
            int(row.predicted_baseline),
        )
        for row in predictions.itertuples(index=False)
    ]


def _policy_effect_rows(effects: pd.DataFrame) -> list[tuple]:
    rows = []
    for row in effects.itertuples(index=False):
        effect_ratio = None if pd.isna(row.effect_ratio) else float(row.effect_ratio)
        rows.append(
            (
                row.scenario,
                row.scope_type,
                row.scope_value,
                int(row.runmile_budget),
                int(row.runmile_used),
                int(row.linked_payment_amount),
                int(row.actual_sales),
                int(row.predicted_baseline),
                int(row.estimated_incremental_sales),
                effect_ratio,
                datetime.fromisoformat(row.created_at),
            )
        )
    return rows


def _load_merchant_ids(
    cursor: psycopg.Cursor,
    merchants: pd.DataFrame,
) -> dict[int, int]:
    source_ids_by_code = {
        f"AI_{merchant.merchant_code}": int(merchant.merchant_id)
        for merchant in merchants.itertuples(index=False)
    }
    cursor.execute(
        "SELECT id, merchant_code FROM merchant WHERE merchant_code = ANY(%s)",
        (list(source_ids_by_code),),
    )
    merchant_ids = {
        source_ids_by_code[merchant_code]: int(merchant_id)
        for merchant_id, merchant_code in cursor.fetchall()
    }
    if len(merchant_ids) != len(source_ids_by_code):
        raise RuntimeError("합성 가맹점 ID 매핑을 완성하지 못했습니다.")
    return merchant_ids


def load_results_to_postgres() -> None:
    merchants = pd.read_csv(SYNTHETIC_DIR / "merchants.csv")
    sales = pd.read_csv(SYNTHETIC_DIR / "merchant_daily_sales.csv")
    predictions = pd.read_csv(PROCESSED_DIR / "ai_predictions.csv")
    effects = pd.read_csv(RESULTS_DIR / "policy_effects.csv")

    with _connect() as connection, connection.cursor() as cursor:
        cursor.executemany(
            """
            INSERT INTO merchant (
                merchant_code, name, district, category, address, runmile_enabled
            ) VALUES (%s, %s, %s, %s, %s, %s)
            ON CONFLICT (merchant_code) DO UPDATE SET
                name = EXCLUDED.name,
                district = EXCLUDED.district,
                category = EXCLUDED.category,
                address = EXCLUDED.address,
                runmile_enabled = EXCLUDED.runmile_enabled
            """,
            _merchant_rows(merchants),
        )
        merchant_ids = _load_merchant_ids(cursor, merchants)
        cursor.executemany(
            """
            INSERT INTO merchant_daily_sales (
                merchant_id, date, sales_amount, transaction_count,
                temperature, rainfall, is_weekend, is_marathon_day, scenario
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (merchant_id, date, scenario) DO UPDATE SET
                sales_amount = EXCLUDED.sales_amount,
                transaction_count = EXCLUDED.transaction_count,
                temperature = EXCLUDED.temperature,
                rainfall = EXCLUDED.rainfall,
                is_weekend = EXCLUDED.is_weekend,
                is_marathon_day = EXCLUDED.is_marathon_day
            """,
            _sales_rows(sales, merchant_ids),
        )
        cursor.executemany(
            """
            INSERT INTO ai_prediction (
                merchant_id, date, scenario, actual_sales, predicted_baseline
            ) VALUES (%s, %s, %s, %s, %s)
            ON CONFLICT (merchant_id, date, scenario) DO UPDATE SET
                actual_sales = EXCLUDED.actual_sales,
                predicted_baseline = EXCLUDED.predicted_baseline,
                created_at = now()
            """,
            _prediction_rows(predictions, merchant_ids),
        )
        cursor.executemany(
            """
            INSERT INTO policy_effect (
                scenario, scope_type, scope_value, runmile_budget, runmile_used,
                linked_payment_amount, actual_sales, predicted_baseline,
                estimated_incremental_sales, effect_ratio, created_at
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (scenario, scope_type, scope_value) DO UPDATE SET
                runmile_budget = EXCLUDED.runmile_budget,
                runmile_used = EXCLUDED.runmile_used,
                linked_payment_amount = EXCLUDED.linked_payment_amount,
                actual_sales = EXCLUDED.actual_sales,
                predicted_baseline = EXCLUDED.predicted_baseline,
                estimated_incremental_sales = EXCLUDED.estimated_incremental_sales,
                effect_ratio = EXCLUDED.effect_ratio,
                created_at = EXCLUDED.created_at
            """,
            _policy_effect_rows(effects),
        )
    print(
        "Loaded "
        f"{len(merchants)} merchants, {len(sales)} sales rows, "
        f"{len(predictions)} predictions, and {len(effects)} policy effects into PostgreSQL"
    )
