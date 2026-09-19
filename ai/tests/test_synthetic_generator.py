from __future__ import annotations

import numpy as np
import pandas as pd

from generator import synthetic_generator


def test_generate_merchants_is_reproducible(monkeypatch) -> None:
    monkeypatch.setattr(synthetic_generator, "DISTRICTS", ("중구",))
    monkeypatch.setattr(synthetic_generator, "MERCHANT_CATEGORIES", ("RESTAURANT",))
    monkeypatch.setattr(synthetic_generator, "MERCHANTS_PER_DISTRICT_CATEGORY", 3)

    first = synthetic_generator.generate_merchants(np.random.default_rng(42))
    second = synthetic_generator.generate_merchants(np.random.default_rng(42))

    pd.testing.assert_frame_equal(first, second)
    assert list(first["group"][:2]) == ["TREATMENT", "CONTROL"]
    assert first["merchant_code"].is_unique


def test_daily_sales_keeps_ground_truth_separate_and_applies_runmile_only_to_treatment(
    monkeypatch,
) -> None:
    monkeypatch.setattr(synthetic_generator, "PRE_EVENT_WEEKS", 1)
    merchants = pd.DataFrame(
        [
            {
                "merchant_id": 1,
                "district": "중구",
                "category": "RESTAURANT",
                "group": "TREATMENT",
                "base_sales": 1_000_000,
            },
            {
                "merchant_id": 2,
                "district": "중구",
                "category": "RESTAURANT",
                "group": "CONTROL",
                "base_sales": 1_000_000,
            },
        ]
    )

    sales, ground_truth = synthetic_generator.generate_daily_sales(
        merchants, np.random.default_rng(42)
    )

    assert set(synthetic_generator.SCENARIOS) == set(sales["scenario"])
    assert "runmile_effect_amount" not in sales.columns
    assert "runmile_used_amount" not in sales.columns

    control_truth = ground_truth[ground_truth["group"] == "CONTROL"]
    assert (control_truth["runmile_effect_amount"] == 0).all()
    assert (control_truth["runmile_used_amount"] == 0).all()

    treatment_truth = ground_truth[ground_truth["group"] == "TREATMENT"].set_index("scenario")
    assert treatment_truth.loc["NONE", "runmile_effect_amount"] == 0
    assert treatment_truth.loc["LOW", "runmile_effect_amount"] > 0
    assert treatment_truth.loc["MEDIUM", "runmile_effect_amount"] > treatment_truth.loc[
        "LOW", "runmile_effect_amount"
    ]
    assert treatment_truth.loc["HIGH", "runmile_effect_amount"] > treatment_truth.loc[
        "MEDIUM", "runmile_effect_amount"
    ]
