from __future__ import annotations

import pandas as pd

from effect.export_policy_effects import _control_corrected_incremental
from training.train_baseline import FEATURE_COLUMNS


def test_baseline_features_exclude_policy_and_ground_truth_values() -> None:
    forbidden = {
        "runmile_amount",
        "runmile_used_amount",
        "runmile_effect_amount",
        "linked_payment_amount",
        "is_marathon_day",
        "scenario",
    }

    assert forbidden.isdisjoint(FEATURE_COLUMNS)


def test_control_correction_subtracts_comparison_group_lift() -> None:
    source = pd.DataFrame(
        [
            {
                "scenario": "MEDIUM",
                "district": "중구",
                "category": "CAFE",
                "group": "CONTROL",
                "actual_sales": 120,
                "predicted_baseline": 100,
            },
            {
                "scenario": "MEDIUM",
                "district": "중구",
                "category": "CAFE",
                "group": "TREATMENT",
                "actual_sales": 160,
                "predicted_baseline": 100,
            },
        ]
    )

    corrected = _control_corrected_incremental(source)
    control = corrected[corrected["group"] == "CONTROL"].iloc[0]
    treatment = corrected[corrected["group"] == "TREATMENT"].iloc[0]

    assert control["estimated_incremental_sales"] == 0
    assert treatment["raw_incremental"] == 60
    assert treatment["control_incremental"] == 20
    assert treatment["estimated_incremental_sales"] == 40
