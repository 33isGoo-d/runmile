# RunMile REST API Specification

## 1. Base URL

All APIs use this prefix:

```text
/api/v1
```

Do not use an unnecessary common success wrapper.

Success responses return the resource JSON directly.

Error response:

```json
{
  "code": "ERROR_CODE",
  "message": "오류 설명"
}
```

Main HTTP statuses:

```text
200 OK
201 Created
400 Bad Request
404 Not Found
409 Conflict
```

## 2. Runner

### GET `/runners/{runnerId}`

Returns participant metadata.

Response:

```json
{
  "id": 1,
  "runnerCode": "RUNNER_00001",
  "course": "FULL"
}
```

## 3. Completion

### GET `/runners/{runnerId}/completion`

Returns completion information.

Response:

```json
{
  "completed": true,
  "course": "FULL",
  "finishTimeSeconds": 12840,
  "completedAt": "2026-02-22T12:30:00"
}
```

## 4. NFT Verification

### GET `/runners/{runnerId}/nft`

Returns marathon NFT verification information.

Response:

```json
{
  "tokenId": "DAEGU-MARATHON-2026-00001",
  "network": "DAEGU_CHAIN_MOCK",
  "verified": true
}
```

Prototype verification is performed by `MockDaeguChainAdapter`.

Frontend must not decide or send NFT verification results.

## 5. RunMile Issue

### POST `/runners/{runnerId}/runmile/issue`

Issues RunMile to a finisher.

Request:

```json
{
  "amount": 10000
}
```

Before issuing RunMile, backend must verify:

```text
completion.completed == true
AND
nft.verified == true
```

Response:

```json
{
  "issuedAmount": 10000,
  "balance": 10000
}
```

Default policy: duplicate issue is not allowed.

If the same completion reward has already been issued:

```text
409 Conflict
```

```json
{
  "code": "RUNMILE_ALREADY_ISSUED",
  "message": "이미 완주 RunMile이 지급되었습니다."
}
```

## 6. Wallet

### GET `/runners/{runnerId}/wallet`

Response:

```json
{
  "balance": 10000,
  "totalIssued": 10000,
  "totalUsed": 0
}
```

## 7. RunMile Transactions

### GET `/runners/{runnerId}/runmile/transactions`

Response:

```json
[
  {
    "id": 1,
    "type": "ISSUE",
    "amount": 10000,
    "paymentId": null,
    "createdAt": "2026-09-18T01:00:00Z"
  },
  {
    "id": 2,
    "type": "USE",
    "amount": 7000,
    "paymentId": 100,
    "createdAt": "2026-09-18T03:30:00Z"
  }
]
```

`createdAt` is an ISO 8601 instant including the UTC offset. The frontend converts it to the viewer's local timezone.

## 8. Merchants

### GET `/merchants`

Returns merchant list.

Optional query parameters:

```text
district
category
runmileEnabled
```

Example:

```text
GET /api/v1/merchants?district=수성구&category=RESTAURANT&runmileEnabled=true
```

Response:

```json
[
  {
    "id": 10,
    "merchantCode": "MERCHANT_00010",
    "name": "RunMile 식당",
    "district": "수성구",
    "category": "RESTAURANT",
    "address": "대구광역시 ...",
    "latitude": 35.84,
    "longitude": 128.68,
    "runmileEnabled": true
  }
]
```

## 9. Payment

### POST `/payments`

Creates a payment that can include RunMile.

Request:

```json
{
  "runnerId": 1,
  "merchantId": 10,
  "totalAmount": 35000,
  "runmileAmount": 10000
}
```

Client does not send `personalAmount`.

Backend calculates:

```text
personalAmount = totalAmount - runmileAmount
```

Validation:

```text
totalAmount > 0
runmileAmount >= 0
runmileAmount <= totalAmount
wallet.balance >= runmileAmount
merchant.runmileEnabled == true
```

On successful payment, process these inside one database transaction:

1. Create Payment
2. Decrease wallet balance
3. Increase wallet totalUsed
4. Create RunMileTransaction USE

Response:

```json
{
  "paymentId": 100,
  "totalAmount": 35000,
  "runmileAmount": 10000,
  "personalAmount": 25000,
  "status": "SUCCESS"
}
```

Insufficient balance:

```text
409 Conflict
```

```json
{
  "code": "INSUFFICIENT_RUNMILE",
  "message": "RunMile 잔액이 부족합니다."
}
```

## 10. Admin Overview

### GET `/admin/analytics/overview`

Returns total policy summary.

Optional query parameter:

```text
scenario=MEDIUM
```

Response:

```json
{
  "scenario": "MEDIUM",
  "runmileBudget": 25000000,
  "runmileUsed": 23100000,
  "linkedPaymentAmount": 74200000,
  "estimatedIncrementalSales": 37400000,
  "effectRatio": 1.50
}
```

`effectRatio` is `null` when a ratio cannot be calculated because the predicted baseline is zero.

Numbers are prototype simulation results and do not represent actual policy performance.

## 11. WHERE - District Analytics

### GET `/admin/analytics/districts`

Returns spending data by district.

Optional query parameter:

```text
scenario=MEDIUM
```

Response:

```json
[
  {
    "district": "중구",
    "runmileUsed": 8300000,
    "linkedPaymentAmount": 25100000,
    "personalPaymentAmount": 16800000,
    "transactionCount": 921,
    "merchantCount": 84
  }
]
```

WHERE is descriptive analytics, not AI analysis.

프로토타입에서 9개 구·군의 실제 결제 표본이 모두 준비되지 않은 경우에는 선택한
시나리오의 overview 총액과 정확히 일치하도록 배분한 합성 데모 응답을 반환한다.
9개 구·군의 실제 결제 집계가 준비되고 overview 총액과 일치하면 실제 집계를 반환한다.
합성 데모 수치는 실제 대구 지역의 정책 성과를 의미하지 않는다.

## 12. Category Analytics

### GET `/admin/analytics/categories`

Returns spending data by category.

Response:

```json
[
  {
    "category": "RESTAURANT",
    "runmileUsed": 9100000,
    "linkedPaymentAmount": 28600000,
    "transactionCount": 1034
  }
]
```

## 13. EFFECT

### GET `/admin/analytics/effects`

Returns AI-estimated policy effects.

Response:

```json
[
  {
    "scopeType": "DISTRICT",
    "scopeValue": "중구",
    "actualSales": 140000000,
    "predictedBaseline": 112000000,
    "estimatedIncrementalSales": 23000000,
    "effectRatio": 1.21
  }
]
```

Each effect's `effectRatio` can be `null` when its predicted baseline is zero.

Important:

```text
actualSales - predictedBaseline
```

is not automatically defined as RunMile effect.

The effect pipeline follows:

```text
Baseline Prediction
+
Comparison Group Correction
→
Estimated Incremental Effect
```

## 14. NEXT Insights

### GET `/admin/analytics/insights`

Returns insight cards generated from policy analysis.

Optional query parameter:

```text
scenario=MEDIUM
```

V1 may use rule-based logic instead of an LLM.

Response:

```json
[
  {
    "type": "CONCENTRATION",
    "title": "소비 집중",
    "description": "중구와 수성구에 전체 RunMile 소비의 52%가 집중되었습니다."
  },
  {
    "type": "LOW_USAGE",
    "title": "저사용 지역",
    "description": "서구의 RunMile 사용률이 전체 평균보다 낮습니다."
  }
]
```

## 15. Common Enum Values

### Course

```text
FULL
TEN_K
FIVE_K
```

### MerchantCategory

```text
RESTAURANT
CAFE
RETAIL
ACCOMMODATION
OTHER
```

### PaymentStatus

```text
SUCCESS
CANCELLED
```

### RunMileTransactionType

```text
ISSUE
USE
CANCEL
```

### Scenario

```text
NONE
LOW
MEDIUM
HIGH
```
