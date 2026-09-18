# AI Methodology

## Architecture

AI does not serve participant-facing real-time requests. Do not add a separate FastAPI AI server for V1.

Use this batch pipeline:

```text
PostgreSQL
     ↓
Python Batch
     ↓
XGBoost Baseline Prediction
     ↓
Comparison Group Analysis
     ↓
Policy Effect Estimation
     ↓
PostgreSQL
     ↓
Spring Analytics API
     ↓
Admin Dashboard
```

The AI section of the dashboard is EFFECT. WHERE is descriptive analytics, and NEXT can be rule-based insight generation in V1.

## Synthetic Data Generator

The generator should create approximately 40,000 synthetic runners and approximately 8 weeks of merchant daily sales. It exists to validate the demo logic when real program data is unavailable.

Use real, legally available data when possible, such as marathon scale, public merchant/category information, and district metadata. Synthesize unavailable personal behavior and financial transaction data.

가맹점은 대구 9개 구·군을 모두 포함한다. 지도 시연용 위도·경도는 각 구·군 중심점에
고정 시드 오프셋을 적용해 생성하며, 실제 사업자 위치로 해석하거나 외부에 사실 데이터로
제공하지 않는다.

Sales simulation formula:

```text
sales = base_sales * weekday_effect * weather_effect * marathon_effect * runmile_effect * noise
```

The simulation must separate marathon effect and RunMile effect so the dashboard can explain whether impact came from the event itself or the reward/payment loop.

Example:

```text
normal sales: 100
marathon effect: 100 -> 120
RunMile effect: 120 -> 145
ground truth marathon effect: +20
ground truth RunMile incremental effect: +25
```

## Scenarios

Use four policy scenarios:

- `NONE`: RunMile effect = 0
- `LOW`: approximately +3% to +8%
- `MEDIUM`: approximately +10% to +20%
- `HIGH`: approximately +20% to +35%

Effect ranges are simulation parameters for validation, not real-world claims.

The model should avoid detecting a false positive effect in the `NONE` scenario.

## Treatment and Control

Use treatment/control groups to estimate incremental spending. Treatment merchants receive marathon and RunMile exposure. Control merchants represent comparable baseline behavior.

The control group should be similar by district, category, and historical average sales when possible.

## XGBoost Baseline

The baseline model predicts normal sales without RunMile. Do not include `runmile_amount` as a model feature because that would leak the treatment signal into the baseline prediction.

Recommended features:

- merchant category
- district
- day of week
- is weekend
- holiday/event indicator
- weather proxy
- temperature
- rainfall
- historical average sales
- previous day sales
- previous week same day sales

## Validation

Generate approximately 8 weeks of pre-event data. A practical split is 6 weeks for training and 2 weeks for validation/test, followed by marathon-day policy effect evaluation.

Train and validate on pre-event periods. Track:

- MAE
- MAPE
- RMSE

Evaluate policy effect with:

```text
ground truth effect vs estimated effect
```

`data/results/ground_truth.csv` is evaluation-only. It must never be used as model input.

배치는 기준 매출 모델의 MAE/MAPE/RMSE를 `data/results/model_metrics.csv`에,
시나리오별 효과 추정 오차를 `data/results/evaluation_report.csv`에 기록한다. 운영 DB에는
대시보드 표시를 위한 집계 지표만 적재하며, 가맹점별 ground truth 행은 적재하지 않는다.

## Dashboard Output

The model output should support dashboard values such as:

- `predictedBaseline`
- `actualSales`
- `estimatedIncrementalSales`
- `effectRatio`
- `scopeType`
- `scopeValue`
- `scenario`

Important:

```text
RunMile Linked Payment != Estimated Incremental Sales
```

The dashboard must not present all RunMile-linked payment as policy-created incremental spending.
