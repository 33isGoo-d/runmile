# RunMile Database Design

## 1. Database

RunMile uses one PostgreSQL database.

Service data and AI analysis results are stored in the same database. Temporary CSV files and ground truth used for AI training/evaluation are kept outside the database.

## 2. Common Principles

### Money

All amounts are stored as integer Korean won values:

```text
BIGINT
```

Do not use floating point values for money.

### Personal Information

The prototype does not store personal information.

Do not store:

- name
- phone number
- card number
- email
- participant address
- other direct participant identifiers

Runner uses anonymous IDs:

```text
RUNNER_00001
RUNNER_00002
```

### Naming Convention

Database and Python:

```text
snake_case
```

Java and TypeScript:

```text
camelCase
```

When changing the schema, check these files together:

```text
database/init.sql
docs/database.md
docs/api-spec.md
```

## 3. Enum Contract

Frontend, Backend, and AI use the same enum-like values.

### Course

```text
FULL
TEN_K
FIVE_K
```

### RunMileTransactionType

```text
ISSUE
USE
CANCEL
```

### PaymentStatus

```text
SUCCESS
CANCELLED
```

### Scenario

```text
NONE
LOW
MEDIUM
HIGH
```

### MerchantCategory

```text
RESTAURANT
CAFE
RETAIL
ACCOMMODATION
OTHER
```

## 4. Entity Relationship

```text
Runner
 │
 ├── Completion
 │      │
 │      └── NFTRecord
 │
 ├── RunMileWallet
 │      │
 │      └── RunMileTransaction
 │
 └── Payment ───────── Merchant
                         │
                         └── MerchantDailySales

Merchant
 │
 └── AIPrediction

PolicyEffect
```

## 5. runner

Represents a participant in anonymous prototype form.

```text
id              BIGINT PK
runner_code     VARCHAR UNIQUE NOT NULL
course          VARCHAR NOT NULL
created_at      TIMESTAMP NOT NULL
```

Example:

```text
runner_code = RUNNER_00001
course      = FULL
```

## 6. completion

Represents marathon completion information.

```text
id                   BIGINT PK
runner_id            BIGINT FK NOT NULL
finish_time_seconds  INTEGER
completed            BOOLEAN NOT NULL
completed_at         TIMESTAMP
```

Relationship:

```text
runner 1 : 1 completion
```

The prototype assumes one runner participates in one race.

## 7. nft_record

Stores marathon NFT verification status.

```text
id               BIGINT PK
runner_id        BIGINT FK UNIQUE NOT NULL
completion_id    BIGINT FK UNIQUE NOT NULL
nft_token_id     VARCHAR UNIQUE NOT NULL
network          VARCHAR NOT NULL
verified         BOOLEAN NOT NULL
issued_at        TIMESTAMP
```

관계:

```text
runner 1 : 1 nft_record
completion 1 : 1 nft_record
```

UNIQUE 제약으로 동일 참가자 또는 동일 완주 기록에 NFT 기록이 중복 생성되는 것을 방지한다.

Prototype example:

```text
network = DAEGU_CHAIN_MOCK
```

Do not store the full blockchain transaction history. The prototype only needs the reward eligibility verification result.

## 8. runmile_wallet

Represents a participant's RunMile wallet.

```text
id             BIGINT PK
runner_id      BIGINT FK UNIQUE NOT NULL

balance        BIGINT NOT NULL
total_issued   BIGINT NOT NULL
total_used     BIGINT NOT NULL

created_at     TIMESTAMP NOT NULL
updated_at     TIMESTAMP NOT NULL
```

Default values:

```text
balance      = 0
total_issued = 0
total_used   = 0
```

## 9. runmile_transaction

RunMile deposit/use ledger.

```text
id             BIGINT PK
wallet_id      BIGINT FK NOT NULL

type           VARCHAR NOT NULL
amount         BIGINT NOT NULL

payment_id     BIGINT NULL

created_at     TIMESTAMP NOT NULL
```

Type:

```text
ISSUE
USE
CANCEL
```

Store positive amounts and interpret increase/decrease by `type`.

Examples:

```text
type   = ISSUE
amount = 10000
```

```text
type   = USE
amount = 10000
```

Wallet balance calculation handles increase/decrease based on transaction type.

## 10. merchant

Stores RunMile-enabled merchant information for the prototype.

```text
id                 BIGINT PK

merchant_code      VARCHAR UNIQUE NOT NULL
name               VARCHAR NOT NULL

district           VARCHAR NOT NULL
category           VARCHAR NOT NULL

address            VARCHAR
latitude           DECIMAL
longitude          DECIMAL

runmile_enabled    BOOLEAN NOT NULL

created_at         TIMESTAMP NOT NULL
```

Example:

```text
district = 수성구
category = RESTAURANT
```

`runmile_enabled` is a prototype simulation field. It does not represent an official Daegu-ro Pay merchant attribute.

## 11. payment

Stores payments from the participant demo.

```text
id                  BIGINT PK

runner_id           BIGINT FK NOT NULL
merchant_id         BIGINT FK NOT NULL

total_amount        BIGINT NOT NULL
runmile_amount      BIGINT NOT NULL
personal_amount     BIGINT NOT NULL

status              VARCHAR NOT NULL

paid_at             TIMESTAMP NOT NULL
```

Always satisfy:

```text
total_amount = runmile_amount + personal_amount
```

Apply a database CHECK constraint.

Additional constraints:

```text
total_amount >= 0
runmile_amount >= 0
personal_amount >= 0
```

## 12. merchant_daily_sales

Daily merchant sales data used for AI baseline prediction.

```text
id                    BIGINT PK

merchant_id           BIGINT FK NOT NULL
date                  DATE NOT NULL

sales_amount          BIGINT NOT NULL
transaction_count     INTEGER NOT NULL

temperature           DOUBLE PRECISION
rainfall              DOUBLE PRECISION

is_weekend            BOOLEAN NOT NULL
is_marathon_day       BOOLEAN NOT NULL

scenario              VARCHAR NOT NULL
```

Scenario:

```text
NONE
LOW
MEDIUM
HIGH
```

Most prototype rows are generated by Synthetic Data Generator.

## 13. ai_prediction

Stores merchant-level baseline prediction generated by AI.

```text
id                     BIGINT PK

merchant_id            BIGINT FK NOT NULL
date                   DATE NOT NULL

scenario               VARCHAR NOT NULL

actual_sales           BIGINT NOT NULL
predicted_baseline     BIGINT NOT NULL

created_at             TIMESTAMP NOT NULL
```

`predicted_baseline` means:

```text
Expected normal sales if the RunMile policy did not exist.
```

## 14. policy_effect

Stores policy-effect aggregate results for the dashboard.

```text
id                           BIGINT PK

scenario                     VARCHAR NOT NULL

scope_type                   VARCHAR NOT NULL
scope_value                  VARCHAR NOT NULL

runmile_budget               BIGINT NOT NULL
runmile_used                 BIGINT NOT NULL
linked_payment_amount        BIGINT NOT NULL

actual_sales                 BIGINT NOT NULL
predicted_baseline           BIGINT NOT NULL

estimated_incremental_sales  BIGINT NOT NULL
effect_ratio                 DOUBLE PRECISION

created_at                   TIMESTAMP NOT NULL

UNIQUE (scenario, scope_type, scope_value)
```

AI 배치를 다시 실행하면 동일한 시나리오·범위 결과를 UPSERT하여 최신 값으로 갱신한다.

Scope examples:

```text
scope_type  = TOTAL
scope_value = ALL
```

```text
scope_type  = DISTRICT
scope_value = 중구
```

```text
scope_type  = CATEGORY
scope_value = RESTAURANT
```

## 15. Ground Truth

The actual policy effect injected by Synthetic Data Generator is not stored in the operational database.

Example:

```text
data/results/ground_truth.csv
```

Ground truth cannot be used as AI model input.

It is only used for evaluation:

```text
Injected Ground Truth
        vs
AI Estimated Effect
```

This checks how accurately the model recovers the policy effect injected by the synthetic generator.
