from __future__ import annotations

import numpy as np
import pandas as pd

from common.config import (
    DISTRICT_CENTERS,
    DISTRICTS,
    MARATHON_DATE,
    MERCHANT_CATEGORIES,
    MERCHANTS_PER_DISTRICT_CATEGORY,
    PRE_EVENT_WEEKS,
    RANDOM_SEED,
    RUNMILE_EFFECT_RANGE,
    SCENARIOS,
    TREATMENT_RATIO,
)

BASE_SALES_BY_CATEGORY = {
    "RESTAURANT": 1_000_000,
    "CAFE": 600_000,
    "RETAIL": 800_000,
    "ACCOMMODATION": 1_500_000,
    "OTHER": 700_000,
}

# Monday=0 ... Sunday=6. Saturday is boosted per the ai-methodology.md example (100 -> 120).
WEEKDAY_EFFECT = {0: 0.95, 1: 0.95, 2: 0.97, 3: 1.0, 4: 1.05, 5: 1.20, 6: 1.10}

# Marathon foot traffic lifts every merchant regardless of RunMile participation.
MARATHON_EFFECT_RANGE = (0.15, 0.25)

# Share of a treatment merchant's marathon-day sales assumed to be RunMile-linked payments.
RUNMILE_USAGE_SHARE_RANGE = (0.15, 0.35)

# Matches the demo payment example in docs/architecture.md (10,000 RunMile of 35,000 total).
AVG_RUNMILE_PAYMENT_RATIO = 10_000 / 35_000


def build_synthetic_preview() -> pd.DataFrame:
    """Small fixed sample kept for smoke-checking the AI batch wiring."""
    return pd.DataFrame(
        [
            {
                "merchant_id": 1,
                "district": "중구",
                "category": "RESTAURANT",
                "scenario": "MEDIUM",
                "base_sales": 1_000_000,
                "estimated_sales": 1_450_000,
            }
        ]
    )


def generate_merchants(rng: np.random.Generator) -> pd.DataFrame:
    rows = []
    merchant_id = 1
    for district in DISTRICTS:
        for category in MERCHANT_CATEGORIES:
            for i in range(MERCHANTS_PER_DISTRICT_CATEGORY):
                if i == 0:
                    group = "TREATMENT"
                elif i == 1:
                    group = "CONTROL"
                else:
                    group = "TREATMENT" if rng.random() < TREATMENT_RATIO else "CONTROL"

                base_sales = BASE_SALES_BY_CATEGORY[category] * rng.uniform(0.7, 1.3)
                center_latitude, center_longitude = DISTRICT_CENTERS[district]
                rows.append(
                    {
                        "merchant_id": merchant_id,
                        "merchant_code": f"MERCHANT_{merchant_id:05d}",
                        "district": district,
                        "category": category,
                        "latitude": round(rng.normal(center_latitude, 0.012), 7),
                        "longitude": round(rng.normal(center_longitude, 0.015), 7),
                        "runmile_enabled": group == "TREATMENT",
                        "group": group,
                        "base_sales": round(base_sales),
                    }
                )
                merchant_id += 1
    return pd.DataFrame(rows)


def _daily_weather(rng: np.random.Generator, n: int) -> pd.DataFrame:
    temperature = rng.normal(5, 6, size=n)  # late-Feb Daegu range
    rainfall = rng.choice([0, 0, 0, 0, 5, 15], size=n)
    weather_effect = 1.0 - (rainfall > 0) * rng.uniform(0.05, 0.12, size=n)
    return pd.DataFrame({"temperature": temperature, "rainfall": rainfall, "weather_effect": weather_effect})


def generate_daily_sales(
    merchants: pd.DataFrame, rng: np.random.Generator
) -> tuple[pd.DataFrame, pd.DataFrame]:
    """Generate pre-event weeks (scenario-independent) plus one marathon day per scenario.

    Returns (merchant_daily_sales rows, ground_truth rows). Ground truth carries the
    injected marathon/RunMile effect amounts and is evaluation-only.
    """
    marathon_date = pd.Timestamp(MARATHON_DATE)
    pre_event_dates = pd.date_range(
        end=marathon_date - pd.Timedelta(days=1), periods=PRE_EVENT_WEEKS * 7, freq="D"
    )

    sales_rows: list[dict] = []
    truth_rows: list[dict] = []

    for _, merchant in merchants.iterrows():
        base_sales = merchant["base_sales"]

        # Pre-event days: identical across scenarios, no marathon/RunMile effect yet.
        weather = _daily_weather(rng, len(pre_event_dates))
        for date, (_, w) in zip(pre_event_dates, weather.iterrows()):
            weekday_effect = WEEKDAY_EFFECT[date.dayofweek]
            noise = rng.normal(1.0, 0.05)
            expected = base_sales * weekday_effect * w["weather_effect"]
            actual = max(round(expected * noise), 0)
            for scenario in SCENARIOS:
                sales_rows.append(
                    {
                        "merchant_id": merchant["merchant_id"],
                        "date": date.date().isoformat(),
                        "sales_amount": actual,
                        "transaction_count": max(round(actual / 15_000), 1),
                        "temperature": round(w["temperature"], 1),
                        "rainfall": round(w["rainfall"], 1),
                        "is_weekend": int(date.dayofweek >= 5),
                        "is_marathon_day": 0,
                        "scenario": scenario,
                    }
                )

        # Marathon day: marathon effect applies to everyone, RunMile effect only to treatment.
        w = _daily_weather(rng, 1).iloc[0]
        weekday_effect = WEEKDAY_EFFECT[marathon_date.dayofweek]
        expected_normal = base_sales * weekday_effect * w["weather_effect"]
        marathon_multiplier = 1.0 + rng.uniform(*MARATHON_EFFECT_RANGE)
        expected_with_marathon = expected_normal * marathon_multiplier
        marathon_effect_amount = round(expected_with_marathon - expected_normal)

        # Draw marathon-day noise once so scenarios represent the same day under
        # different policies, not four independent random days.
        marathon_day_noise = rng.normal(1.0, 0.05)

        for scenario in SCENARIOS:
            if merchant["group"] == "TREATMENT":
                low, high = RUNMILE_EFFECT_RANGE[scenario]
                runmile_multiplier = 1.0 + (rng.uniform(low, high) if high > 0 else 0.0)
            else:
                runmile_multiplier = 1.0

            expected_with_runmile = expected_with_marathon * runmile_multiplier
            runmile_effect_amount = round(expected_with_runmile - expected_with_marathon)

            actual = max(round(expected_with_runmile * marathon_day_noise), 0)

            sales_rows.append(
                {
                    "merchant_id": merchant["merchant_id"],
                    "date": marathon_date.date().isoformat(),
                    "sales_amount": actual,
                    "transaction_count": max(round(actual / 15_000), 1),
                    "temperature": round(w["temperature"], 1),
                    "rainfall": round(w["rainfall"], 1),
                    "is_weekend": int(marathon_date.dayofweek >= 5),
                    "is_marathon_day": 1,
                    "scenario": scenario,
                }
            )

            if merchant["group"] == "TREATMENT":
                usage_share = rng.uniform(*RUNMILE_USAGE_SHARE_RANGE)
                linked_payment_amount = round(actual * usage_share)
                runmile_used_amount = round(linked_payment_amount * AVG_RUNMILE_PAYMENT_RATIO)
            else:
                linked_payment_amount = 0
                runmile_used_amount = 0

            truth_rows.append(
                {
                    "merchant_id": merchant["merchant_id"],
                    "date": marathon_date.date().isoformat(),
                    "scenario": scenario,
                    "district": merchant["district"],
                    "category": merchant["category"],
                    "group": merchant["group"],
                    "expected_sales_without_event": round(expected_normal),
                    "marathon_effect_amount": marathon_effect_amount,
                    "runmile_effect_amount": runmile_effect_amount,
                    "actual_sales": actual,
                    "linked_payment_amount": linked_payment_amount,
                    "runmile_used_amount": runmile_used_amount,
                }
            )

    return pd.DataFrame(sales_rows), pd.DataFrame(truth_rows)


def generate_all(seed: int = RANDOM_SEED) -> dict[str, pd.DataFrame]:
    rng = np.random.default_rng(seed)
    merchants = generate_merchants(rng)
    daily_sales, ground_truth = generate_daily_sales(merchants, rng)
    return {
        "merchants": merchants,
        "merchant_daily_sales": daily_sales,
        "ground_truth": ground_truth,
    }
