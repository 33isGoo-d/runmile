from __future__ import annotations

from datetime import datetime, timezone

import pandas as pd

from common.config import RESULTS_DIR, SCENARIOS


def evaluate_effect() -> pd.DataFrame:
    """Compare estimated effects with data/results/ground_truth.csv.

    This is evaluation-only: it checks how well the AI pipeline recovers the
    RunMile effect the synthetic generator injected, per scenario.
    """
    ground_truth = pd.read_csv(RESULTS_DIR / "ground_truth.csv")
    policy_effects = pd.read_csv(RESULTS_DIR / "policy_effects.csv")

    injected = (
        ground_truth[ground_truth["group"] == "TREATMENT"]
        .groupby("scenario")["runmile_effect_amount"]
        .sum()
        .rename("injected_effect")
    )
    estimated = (
        policy_effects[policy_effects["scope_type"] == "TOTAL"]
        .set_index("scenario")["estimated_incremental_sales"]
        .rename("estimated_effect")
    )

    report = pd.concat([injected, estimated], axis=1).reindex(list(SCENARIOS)).reset_index()
    report["difference"] = report["estimated_effect"] - report["injected_effect"]
    report["difference_pct"] = (
        report["difference"] / report["injected_effect"].where(report["injected_effect"] != 0) * 100
    ).round(1)
    report["evaluated_at"] = datetime.now(timezone.utc).isoformat()

    RESULTS_DIR.mkdir(parents=True, exist_ok=True)
    output_path = RESULTS_DIR / "evaluation_report.csv"
    report.to_csv(output_path, index=False)
    print(report.to_string(index=False))

    none_row = report.loc[report["scenario"] == "NONE", "estimated_effect"]
    max_injected = injected.abs().max()
    if not none_row.empty and max_injected and abs(none_row.iloc[0]) > max_injected * 0.1:
        print("Warning: NONE scenario shows a non-trivial estimated effect (possible false positive).")

    return report
