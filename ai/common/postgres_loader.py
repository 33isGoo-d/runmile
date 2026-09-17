from __future__ import annotations

from datetime import datetime

import pandas as pd
import psycopg

from common.config import (
    AI_MERCHANT_ID_OFFSET,
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
        merchant_id = int(merchant.merchant_id) + AI_MERCHANT_ID_OFFSET
        rows.append(
            (
                merchant_id,
                f"AI_{merchant.merchant_code}",
                f"합성 {merchant.district} {merchant.category} {merchant.merchant_id}",
                merchant.district,
                merchant.category,
                f"대구광역시 {merchant.district}",
                bool(merchant.runmile_enabled),
            )
        )
    return rows


def _sales_rows(sales: pd.DataFrame) -> list[tuple]:
    return [
        (
            int(row.merchant_id) + AI_MERCHANT_ID_OFFSET,
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


def _prediction_rows(predictions: pd.DataFrame) -> list[tuple]:
    return [
        (
            int(row.merchant_id) + AI_MERCHANT_ID_OFFSET,
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


def load_results_to_postgres() -> None:
    merchants = pd.read_csv(SYNTHETIC_DIR / "merchants.csv")
    sales = pd.read_csv(SYNTHETIC_DIR / "merchant_daily_sales.csv")
    predictions = pd.read_csv(PROCESSED_DIR / "ai_predictions.csv")
    effects = pd.read_csv(RESULTS_DIR / "policy_effects.csv")

    with _connect() as connection, connection.cursor() as cursor:
        cursor.executemany(
            """
            INSERT INTO merchant (
                id, merchant_code, name, district, category, address, runmile_enabled
            ) VALUES (%s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (id) DO UPDATE SET
                merchant_code = EXCLUDED.merchant_code,
                name = EXCLUDED.name,
                district = EXCLUDED.district,
                category = EXCLUDED.category,
                address = EXCLUDED.address,
                runmile_enabled = EXCLUDED.runmile_enabled
            """,
            _merchant_rows(merchants),
        )
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
            _sales_rows(sales),
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
            _prediction_rows(predictions),
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
        cursor.execute(
            "SELECT setval(pg_get_serial_sequence('merchant', 'id'), "
            "GREATEST((SELECT MAX(id) FROM merchant), 1), true)"
        )

    print(
        "Loaded "
        f"{len(merchants)} merchants, {len(sales)} sales rows, "
        f"{len(predictions)} predictions, and {len(effects)} policy effects into PostgreSQL"
    )
