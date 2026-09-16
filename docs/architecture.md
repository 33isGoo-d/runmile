# RunMile System Architecture

## 1. Document Purpose

This document defines the overall system structure and component responsibilities for the RunMile prototype.

When adding new features or structure, follow the service boundaries and technology roles in this document first.

RunMile is not a project to build a real financial service. It is an end-to-end prototype that demonstrates how a Daegu Marathon completion incentive can be connected to local spending and measured for estimated policy impact.

## 2. Service Definition

RunMile verifies Daegu Marathon completion through a blockchain-style proof, provides finishers with a local consumption incentive, connects that incentive to Daegu small-business spending, and uses AI to estimate incremental policy impact for next-year city and budget decisions.

Core structure:

```text
Action
  ↓
Reward
  ↓
Spending
  ↓
Measurement
  ↓
Policy Improvement
```

Overall service flow:

```text
Daegu Marathon completion
        ↓
Daegu Chain NFT/DID-based completion verification
        ↓
RunMile issue
        ↓
Local spending designed to connect with Daegu-ro Pay/iM Shop infrastructure
        ↓
Payment and spending data
        ↓
AI policy-effect analysis
        ↓
RunMile Impact Dashboard
        ↓
Next-year policy and budget decision
```

## 3. Core Problem

Large regional events such as the Daegu Marathon can bring participants and visitors into local commercial districts.

However, an increase in local sales on marathon day does not prove that a specific incentive policy created that increase. If a restaurant usually earns 1,000,000 KRW and earns 1,500,000 KRW on marathon day, the full 500,000 KRW increase cannot be attributed to RunMile because the marathon itself also creates visitor demand.

RunMile focuses on this question:

```text
How much additional local spending did the incentive policy create, by district and category?
```

RunMile is not just a marathon point service. It is a system for estimating policy impact and creating evidence for the next policy and budget cycle.

The platform must distinguish:

- event effect from the marathon itself
- incentive effect from RunMile
- total linked payment volume
- estimated incremental spending

## 4. Technology Roles

### 4.1 Blockchain

Blockchain answers this question:

```text
Did this participant complete the marathon and qualify for RunMile?
```

Responsibilities:

- completion-record trust
- NFT/DID-based reward eligibility verification

RunMile itself is not issued as a blockchain token. The project does not build a new blockchain network.

The prototype uses:

```text
BlockchainVerificationPort
          │
          ▼
MockDaeguChainAdapter
```

If real integration becomes available later, the external adapter can be replaced:

```text
BlockchainVerificationPort
          │
          ▼
DaeguChainAdapter
```

### 4.2 RunMile

RunMile is:

```text
A purpose-bound local consumption incentive issued to Daegu Marathon finishers.
```

RunMile is not:

- cryptocurrency
- blockchain token
- new local currency
- independent prepaid payment instrument

User-facing UX can display:

```text
10,000 RunMile
```

For real business deployment, the design targets connection with existing Daegu-ro Pay/iM Shop issue, payment, merchant, and settlement infrastructure.

### 4.3 Financial Infrastructure

Financial infrastructure answers this question:

```text
How does a marathon reward become actual local spending in Daegu?
```

Target business structure:

```text
RunMile
   ↓
Daegu-ro Pay / iM Shop
   ↓
Daegu local merchants
   ↓
Payment / settlement
```

The prototype does not connect to a real financial system. It uses:

```text
PaymentGatewayPort
        │
        ▼
MockDaeguPayAdapter
```

Do not claim that this prototype can access real Daegu-ro Pay APIs or iM Bank financial data.

### 4.4 AI

AI answers this question:

```text
What would sales have been without the RunMile policy?
```

AI predicts normal baseline sales, applies comparison-group correction, and estimates incremental RunMile policy impact.

Use careful wording:

- estimated policy impact
- estimated incremental sales
- AI-estimated effect

Do not claim perfect causal proof.

## 5. Overall Software Architecture

```text
┌───────────────────────────────────┐
│          Participant Web          │
│       Next.js / TypeScript        │
└─────────────────┬─────────────────┘
                  │ REST API
                  ▼
┌───────────────────────────────────┐
│          Spring Boot API          │
│                                   │
│ Runner                            │
│ Completion                        │
│ RunMile Wallet                    │
│ Merchant                          │
│ Payment                           │
│ Analytics                         │
└───────┬─────────────────┬─────────┘
        │                 │
        ▼                 ▼
 MockDaeguChain      MockDaeguPay
   Adapter             Adapter
        │                 │
        └────────┬────────┘
                 ▼
          ┌─────────────┐
          │ PostgreSQL  │
          └──────┬──────┘
                 │
                 ▼
       ┌──────────────────┐
       │ Python AI Batch  │
       │                  │
       │ Synthetic Data   │
       │ XGBoost          │
       │ Effect Analysis  │
       └────────┬─────────┘
                │
                ▼
          ┌─────────────┐
          │ PostgreSQL  │
          │ AI Results  │
          └──────┬──────┘
                 │
                 ▼
┌───────────────────────────────────┐
│      RunMile Impact Dashboard     │
│                                   │
│      WHERE -> EFFECT -> NEXT      │
└───────────────────────────────────┘
```

## 6. Participant Service

Participant Service implements this flow:

```text
Completion check
 ↓
NFT verification
 ↓
RunMile issue
 ↓
Wallet check
 ↓
Merchant search
 ↓
RunMile use
 ↓
Payment complete
```

Core screens:

1. Completion/NFT screen
2. RunMile Wallet
3. Merchant map
4. Payment Demo

Example payment:

```text
Total payment       35,000 KRW
RunMile used        10,000 KRW
Personal payment    25,000 KRW
```

Always satisfy:

```text
totalAmount = runmileAmount + personalAmount
```

## 7. RunMile Impact Dashboard

The admin dashboard assumes Daegu city policy managers and iM Bank stakeholders as primary users.

The structure is fixed:

```text
WHERE -> EFFECT -> NEXT
```

### WHERE

Question:

```text
Where did RunMile spending occur?
```

Main metrics:

- RunMile used by district
- linked total payment by district
- personal payment amount
- payment count
- participating merchant count
- spending by merchant category

Main UI:

- Daegu district heatmap
- district/category charts

WHERE is descriptive analytics. Do not describe it as an AI feature.

### EFFECT

Question:

```text
How much additional spending is estimated to have been created by the RunMile policy?
```

Core structure:

```text
AI Baseline Prediction
        +
Comparison Group Correction
        ↓
Estimated Incremental Effect
```

Always distinguish:

```text
RunMile Linked Payment != Estimated Incremental Sales
```

Do not present all RunMile-linked payment volume as economic impact created by RunMile.

### NEXT

Question:

```text
What should be improved in the next policy?
```

Main analyses:

- spending concentration
- low-usage districts
- high-effect categories
- spending blind spots

V1 does not implement complex prescriptive AI. Use rule-based insight cards from existing analysis results.

## 8. AI Architecture

AI does not need to process real-time user requests, so do not add a separate FastAPI AI server.

Use this batch architecture:

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

## 9. Backend Architecture

Backend uses Spring Boot with a package-by-feature structure:

```text
runner/
completion/
wallet/
merchant/
payment/
analytics/

infrastructure/
├── blockchain/
└── payment/

global/
```

Do not use microservices. Keep only minimal port/adapter boundaries for external systems.

## 10. Development Principles

This is a short-term competition prototype.

Follow:

```text
Simple > Clever
Demo > Infrastructure
Working End-to-End Flow > Feature Count
```

Do not implement:

- real financial payment
- real Daegu Chain integration
- new blockchain
- new local currency
- RunMile token
- microservices
- Kafka
- Redis
- Elasticsearch
- Kubernetes
- complex authentication
- Participant AI chatbot
- LLM report generator
- complex deep learning
- automatic budget optimization

## 11. Success Criteria

The prototype is successful when this end-to-end demo works:

```text
Daegu Marathon completion
 ↓
NFT VERIFIED
 ↓
+10,000 RunMile
 ↓
local merchant selected
 ↓
35,000 KRW payment
 ├─ RunMile 10,000
 └─ personal payment 25,000
 ↓
Transaction saved
 ↓
Impact Dashboard
 ↓
WHERE
 ↓
EFFECT
 ↓
AI Estimated Incremental Effect
 ↓
NEXT
```

Core message:

```text
Blockchain proves action, finance converts action into spending, and AI estimates the policy impact of that spending.
```
