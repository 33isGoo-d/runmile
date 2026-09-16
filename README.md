# RunMile

Daegu Marathon completion rewards participants with mock RunMile points, connects them to local merchants, and analyzes estimated local spending impact with batch AI.

## One-Line Definition

RunMile verifies Daegu Marathon completion, issues purpose-bound local consumption incentives, connects them to local merchant spending, and helps Daegu/iM Bank decision makers estimate incremental policy impact for next-year budgeting.

Core loop:

```text
Action -> Reward -> Spending -> Measurement -> Policy Improvement
```

## Prototype Goal

The team has a very short development window until 2026-09-20. The goal is an end-to-end competition prototype that can be demonstrated clearly.

This is not a production banking system. Prefer direct, understandable implementation over complex infrastructure.

## Stack

- Frontend: Node.js 26.8.2, npm 12.0.2, Next.js 16.3.5, React 19.3.0, TypeScript 5.5.3, Tailwind CSS 3.4.4
- Backend: Java 17, Gradle Wrapper 8.10.2, Spring Boot 3.3.2, Spring Data JPA
- Local tools: Gradle 9.7.1 installed via Homebrew, Docker 29.0.1, Docker Compose v2.40.3
- Database: PostgreSQL 16 via Docker Compose
- AI/Data: Python 3.12.14, Pandas 2.2.2, NumPy 2.0.0, scikit-learn 1.5.1, XGBoost 2.1.0
- API: REST under `/api/v1`
- Map: Kakao Map planned
- Blockchain/Payment: Mock adapters only for this demo

## Directory Structure

```text
runmile/
├── frontend/       # Participant Web and Admin Dashboard
├── backend/        # Spring Boot REST API
├── ai/             # Synthetic data, training, evaluation, effect analysis
├── data/           # Raw, synthetic, processed, and result files
├── database/       # PostgreSQL schema and seed data
└── docs/           # Architecture, API, DB, and AI methodology docs
```

## Collaboration

Team workflow rules live in:

```text
docs/development-workflow.md
```

Use short branch names such as:

```text
feat/be/1-wallet
feat/fe/3-participant-flow
feat/ai/5-synthetic-generator
```

## Quick Start

```bash
cd /Users/leejeongmin/runmile/frontend
npm install
npm run dev
```

```bash
cd /Users/leejeongmin/runmile/backend
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew bootRun
```

```bash
cd /Users/leejeongmin/runmile/ai
/opt/homebrew/bin/python3.12 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python main.py
```

Start local PostgreSQL:

```bash
cd /Users/leejeongmin/runmile
docker compose up -d
```

The backend is verified with Java 17. If your default `java` points to a newer JDK, set `JAVA_HOME` as shown above before using `./gradlew`.

## Demo Principles

- Do not store personal information. Runner data is represented by demo identifiers and aggregate fields only.
- Daegu Chain and Daegu-ro Pay are mock adapters. Do not claim real blockchain or payment integration is complete.
- Describe future financial integration as "designed to connect with Daegu-ro Pay/iM Shop infrastructure," not as completed access.
- AI runs as a batch process and writes analysis results to PostgreSQL. It is not a real-time prediction API.
- Synthetic generator ground truth is evaluation-only and belongs in `data/results/ground_truth.csv`, not in the operational database.
- API success responses return direct JSON. Errors use `{ "code": "...", "message": "..." }`.
- RunMile is a purpose-bound local spending incentive, not a cryptocurrency, new token, independent local currency, or new prepaid payment instrument.

## MVP Priorities

P0:

- Participant demo
- completion and NFT mock verification
- RunMile wallet, issue, use, and transaction
- merchant list and payment mock
- synthetic data generator
- XGBoost baseline
- comparison-group effect estimation
- ground truth evaluation
- Admin WHERE, EFFECT, NEXT dashboard

P1:

- real public merchant/event data where legally available
- Kakao Map polish
- weather data
- AI performance visualization
- scenario demo
- UI polish

P2:

- real blockchain deployment
- real payment or banking integration
- new local currency, RunMile token, or cryptocurrency
- participant AI chatbot
- separate FastAPI AI server
- unnecessary auth/JWT, Redis, Kafka, Kubernetes, or microservices

## Final Demo Story

Participant:

```text
Daegu Marathon completion
-> NFT VERIFIED
-> +10,000 RunMile
-> merchant search
-> 35,000 KRW payment
   - RunMile 10,000
   - personal payment 25,000
-> transaction saved
```

Admin:

```text
RunMile Impact Dashboard
-> WHERE: where RunMile was used
-> EFFECT: estimated incremental spending
-> NEXT: insight cards for next policy decisions
```
