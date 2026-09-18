from __future__ import annotations

from datetime import datetime, timezone

import numpy as np
import pandas as pd
from sklearn.metrics import mean_absolute_error, mean_absolute_percentage_error, mean_squared_error
from xgboost import XGBRegressor

from common.config import CATEGORY_CODE, DISTRICT_CODE, MARATHON_DATE, MODELS_DIR, PROCESSED_DIR, RESULTS_DIR, SYNTHETIC_DIR

# Baseline must predict normal sales "as if RunMile did not exist", so it excludes
# runmile_amount and any event/marathon indicator that only has signal on the one
# marathon day in the dataset.
FEATURE_COLUMNS = [
    "district",
    "category",
    "day_of_week",
    "is_weekend",
    "temperature",
    "rainfall",
    "historical_avg_sales",
    "previous_day_sales",
    "previous_week_same_day_sales",
]
TARGET_COLUMN = "sales_amount"


def _load_dataset() -> pd.DataFrame:
    sales = pd.read_csv(SYNTHETIC_DIR / "merchant_daily_sales.csv", parse_dates=["date"])
    merchants = pd.read_csv(SYNTHETIC_DIR / "merchants.csv")
    df = sales.merge(merchants[["merchant_id", "district", "category"]], on="merchant_id")
    df = df.sort_values(["merchant_id", "scenario", "date"]).reset_index(drop=True)
    df["day_of_week"] = df["date"].dt.dayofweek
    return df


def _add_history_features(df: pd.DataFrame) -> pd.DataFrame:
    grouped = df.groupby(["merchant_id", "scenario"])["sales_amount"]
    df["previous_day_sales"] = grouped.shift(1)
    df["previous_week_same_day_sales"] = grouped.shift(7)
    df["historical_avg_sales"] = grouped.transform(lambda s: s.shift(1).expanding().mean())
    return df


def _encode_categoricals(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    df["district"] = df["district"].map(DISTRICT_CODE)
    df["category"] = df["category"].map(CATEGORY_CODE)
    return df


def train_baseline() -> pd.DataFrame:
    """Train an XGBoost baseline that predicts normal sales without RunMile features."""
    df = _load_dataset()
    df = _add_history_features(df)

    marathon_date = pd.Timestamp(MARATHON_DATE)
    pre_event = df[df["date"] < marathon_date].dropna(subset=FEATURE_COLUMNS)

    # Pre-event rows are identical across scenarios; train on one to avoid counting
    # the same calendar day four times.
    train_source = pre_event[pre_event["scenario"] == "NONE"].sort_values("date")
    cutoff = train_source["date"].quantile(0.75)
    train_df = _encode_categoricals(train_source[train_source["date"] <= cutoff])
    val_df = _encode_categoricals(train_source[train_source["date"] > cutoff])

    model = XGBRegressor(
        n_estimators=200,
        max_depth=4,
        learning_rate=0.08,
        subsample=0.9,
        colsample_bytree=0.9,
        random_state=42,
    )
    model.fit(train_df[FEATURE_COLUMNS], train_df[TARGET_COLUMN])

    val_pred = model.predict(val_df[FEATURE_COLUMNS])
    mae = mean_absolute_error(val_df[TARGET_COLUMN], val_pred)
    mape = mean_absolute_percentage_error(val_df[TARGET_COLUMN], val_pred)
    rmse = mean_squared_error(val_df[TARGET_COLUMN], val_pred) ** 0.5
    print(f"Validation MAE={mae:.0f} MAPE={mape:.3f} RMSE={rmse:.0f}")

    evaluated_at = datetime.now(timezone.utc).isoformat()
    metrics = pd.DataFrame(
        [
            {"metric_name": "MAE", "metric_value": mae, "evaluated_at": evaluated_at},
            {"metric_name": "MAPE", "metric_value": mape, "evaluated_at": evaluated_at},
            {"metric_name": "RMSE", "metric_value": rmse, "evaluated_at": evaluated_at},
        ]
    )
    RESULTS_DIR.mkdir(parents=True, exist_ok=True)
    metrics.to_csv(RESULTS_DIR / "model_metrics.csv", index=False)

    MODELS_DIR.mkdir(parents=True, exist_ok=True)
    model.save_model(MODELS_DIR / "baseline_xgb.json")

    predict_rows = df.dropna(subset=FEATURE_COLUMNS)
    encoded_all = _encode_categoricals(predict_rows)
    predicted_baseline = model.predict(encoded_all[FEATURE_COLUMNS])

    result = predict_rows[["merchant_id", "date", "scenario", "sales_amount"]].copy()
    result["date"] = result["date"].dt.date.astype(str)
    result = result.rename(columns={"sales_amount": "actual_sales"})
    result["predicted_baseline"] = np.round(predicted_baseline).astype(int)

    PROCESSED_DIR.mkdir(parents=True, exist_ok=True)
    output_path = PROCESSED_DIR / "ai_predictions.csv"
    result.to_csv(output_path, index=False)
    print(f"Wrote {len(result)} predictions to {output_path}")
    return result
