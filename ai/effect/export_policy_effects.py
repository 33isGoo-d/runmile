from __future__ import annotations

from datetime import datetime, timezone

import pandas as pd

from common.config import MARATHON_DATE, PROCESSED_DIR, RESULTS_DIR, RUNMILE_BUDGET_BY_SCENARIO, SCENARIOS, SYNTHETIC_DIR

POLICY_EFFECT_COLUMNS = [
    "scenario",
    "scope_type",
    "scope_value",
    "runmile_budget",
    "runmile_used",
    "linked_payment_amount",
    "actual_sales",
    "predicted_baseline",
    "estimated_incremental_sales",
    "effect_ratio",
    "created_at",
]


def _load_marathon_day_predictions() -> pd.DataFrame:
    predictions = pd.read_csv(PROCESSED_DIR / "ai_predictions.csv")
    predictions = predictions[predictions["date"] == MARATHON_DATE]

    merchants = pd.read_csv(SYNTHETIC_DIR / "merchants.csv")
    ground_truth = pd.read_csv(RESULTS_DIR / "ground_truth.csv")

    df = predictions.merge(
        ground_truth[["merchant_id", "scenario", "linked_payment_amount", "runmile_used_amount"]],
        on=["merchant_id", "scenario"],
        how="inner",
    )
    return df.merge(merchants[["merchant_id", "district", "category", "group"]], on="merchant_id")


def _control_corrected_incremental(df: pd.DataFrame) -> pd.DataFrame:
    """Isolate the RunMile-only effect by subtracting the marathon-only lift that
    comparable control merchants show over their own baseline."""
    df = df.copy()
    df["raw_incremental"] = df["actual_sales"] - df["predicted_baseline"]
    control = df[df["group"] == "CONTROL"]

    fine = control.groupby(["scenario", "district", "category"])["raw_incremental"].mean()
    by_category = control.groupby(["scenario", "category"])["raw_incremental"].mean()
    overall = control.groupby(["scenario"])["raw_incremental"].mean()

    def control_baseline(row: pd.Series) -> float:
        fine_key = (row["scenario"], row["district"], row["category"])
        if fine_key in fine.index:
            return fine[fine_key]
        category_key = (row["scenario"], row["category"])
        if category_key in by_category.index:
            return by_category[category_key]
        return overall.get(row["scenario"], 0.0)

    df["control_incremental"] = df.apply(control_baseline, axis=1)
    df["estimated_incremental_sales"] = df["raw_incremental"] - df["control_incremental"]
    # Control merchants are not RunMile-enabled, so they carry no measured RunMile effect.
    df.loc[df["group"] == "CONTROL", "estimated_incremental_sales"] = 0
    return df


def _aggregate(df: pd.DataFrame, scenario: str, scope_type: str, scope_col: str | None) -> pd.DataFrame:
    treatment = df[(df["scenario"] == scenario) & (df["group"] == "TREATMENT")].copy()
    if scope_col is None:
        treatment["_scope"] = "ALL"
        scope_col = "_scope"

    total_used = treatment["runmile_used_amount"].sum()
    grouped = (
        treatment.groupby(scope_col)
        .agg(
            runmile_used=("runmile_used_amount", "sum"),
            linked_payment_amount=("linked_payment_amount", "sum"),
            actual_sales=("actual_sales", "sum"),
            predicted_baseline=("predicted_baseline", "sum"),
            estimated_incremental_sales=("estimated_incremental_sales", "sum"),
        )
        .reset_index()
        .rename(columns={scope_col: "scope_value"})
    )

    scenario_budget = RUNMILE_BUDGET_BY_SCENARIO[scenario]
    if scope_type == "TOTAL":
        grouped["runmile_budget"] = scenario_budget
    elif total_used:
        grouped["runmile_budget"] = (grouped["runmile_used"] / total_used * scenario_budget).round().astype(int)
    else:
        grouped["runmile_budget"] = 0

    grouped["scenario"] = scenario
    grouped["scope_type"] = scope_type
    grouped["effect_ratio"] = (
        (grouped["predicted_baseline"] + grouped["estimated_incremental_sales"])
        / grouped["predicted_baseline"].replace(0, pd.NA)
    ).round(2)
    return grouped[
        [
            "scenario",
            "scope_type",
            "scope_value",
            "runmile_budget",
            "runmile_used",
            "linked_payment_amount",
            "actual_sales",
            "predicted_baseline",
            "estimated_incremental_sales",
            "effect_ratio",
        ]
    ]


def export_policy_effects() -> pd.DataFrame:
    """Export batch AI results for backend/database import.

    Writes data/results/policy_effects.csv in the shape of the `policy_effect`
    table (docs/database.md). Loading this into Postgres is left to whoever wires
    up the analytics API's mock-to-real transition.
    """
    df = _load_marathon_day_predictions()
    df = _control_corrected_incremental(df)

    frames = []
    for scenario in SCENARIOS:
        frames.append(_aggregate(df, scenario, "TOTAL", None))
        frames.append(_aggregate(df, scenario, "DISTRICT", "district"))
        frames.append(_aggregate(df, scenario, "CATEGORY", "category"))

    result = pd.concat(frames, ignore_index=True)
    result["created_at"] = datetime.now(timezone.utc).isoformat()
    result = result[POLICY_EFFECT_COLUMNS]

    RESULTS_DIR.mkdir(parents=True, exist_ok=True)
    output_path = RESULTS_DIR / "policy_effects.csv"
    result.to_csv(output_path, index=False)
    print(f"Wrote {len(result)} policy_effect rows to {output_path}")
    return result
