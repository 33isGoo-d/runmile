import pandas as pd


def build_synthetic_preview() -> pd.DataFrame:
    return pd.DataFrame(
        [
            {
                "merchant_id": 1,
                "district": "JUNG_GU",
                "category": "RESTAURANT",
                "scenario": "MEDIUM",
                "base_sales": 25000,
                "estimated_sales": 35000,
            }
        ]
    )

