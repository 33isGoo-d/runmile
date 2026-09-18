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
- Map: Kakao Maps JavaScript SDK
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

아래 순서대로 실행하면 합성 데이터 생성부터 참가자/관리자 화면까지 한 번에 확인할 수 있습니다.

### 1. PostgreSQL 실행

```bash
cd /Users/leejeongmin/runmile
docker compose up -d
```

최초 실행 시 `database/init.sql`과 `database/seed.sql`이 자동 적용됩니다.

### 2. AI 배치 실행

macOS에서는 XGBoost 실행에 OpenMP 런타임이 필요합니다.

```bash
brew install libomp
cd /Users/leejeongmin/runmile/ai
/opt/homebrew/bin/python3.12 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python main.py
```

배치는 합성 데이터와 평가 결과를 만들고, 가맹점·일별 매출·정상매출 예측·정책 효과를 PostgreSQL에 UPSERT합니다. 재실행 시 기존 운영성 데이터를 삭제하지 않습니다.

### 3. Backend 실행

```bash
cd /Users/leejeongmin/runmile/backend
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew bootRun
```

Backend API는 `http://localhost:8080/api/v1`에서 제공됩니다.

### 4. Frontend 실행

```bash
cd /Users/leejeongmin/runmile/frontend
npm install
npm run dev
```

루트 `.env.example`을 참고해 Frontend 환경 변수에 Kakao Maps JavaScript 키를
설정하면 참가자 화면에서 사용처 지도를 확인할 수 있습니다.

참가자 화면은 `http://localhost:3000/participant`, 관리자 화면은 `http://localhost:3000/admin`에서 확인합니다.

Backend는 Java 17에서 검증했습니다. 기본 `java`가 다른 JDK를 가리키면 `./gradlew` 실행 전에 위와 같이 `JAVA_HOME`을 설정합니다.

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
- weather data
- AI performance visualization
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
