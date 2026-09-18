# RunMile

대구마라톤 완주자에게 지역 소비 목적의 RunMile을 지급하고, 사용처 결제와 지역경제 효과 분석까지 연결하는 프로토타입입니다.

```text
마라톤 완주 -> 완주 증명 검증 -> RunMile 지급 -> 지역 가맹점 사용
             -> 소비 데이터 집계 -> 정책 효과 분석 -> 다음 정책 의사결정
```

개발 목표일은 2026년 9월 20일입니다. 3명이 작은 Issue와 PR 단위로 병렬 개발하며, 복잡한 운영 인프라보다 시연 가능한 전체 흐름을 우선합니다.

## 현재 구현 범위

- 참가자 조회, 완주 기록 및 완주 증명 조회
- RunMile 지갑, 지급, 사용 및 거래 내역
- 가맹점 검색과 Mock 결제
- Kakao Maps 기반 가맹점 지도
- 약 4만 명 규모 합성 데이터와 약 8주 일별 매출 생성
- XGBoost 정상매출 예측과 정책 효과 평가
- 관리자 분석 대시보드
- Polygon Amoy 테스트넷 완주 증명 앵커링 및 온체인 검증

RunMile은 암호화폐나 신규 지역화폐가 아닙니다. 대구로페이 결제는 Mock이며, Polygon Amoy 연동도 공식 대구체인 연동이나 운영망 배포가 아닙니다.

## 기술 스택

| 영역 | 기술 |
|---|---|
| Frontend | Node.js 26.8.2, npm 12.0.2, Next.js 16.3.5, React 19.3.0, TypeScript 5.5.3, Tailwind CSS 3.4.4 |
| Backend | Java 17, Gradle Wrapper 8.10.2, Spring Boot 3.3.2, Spring Data JPA |
| Database | PostgreSQL 16, Docker Compose |
| AI/Data | Python 3.12.14, Pandas 2.2.2, NumPy 2.0.0, scikit-learn 1.5.1, XGBoost 2.1.0 |
| Blockchain | web3j 4.12.3, Polygon Amoy 테스트넷 |
| Map | Kakao Maps JavaScript SDK |
| API | REST, `/api/v1` |

## 디렉터리 구조

```text
runmile/
├── frontend/       # 참가자 화면과 관리자 대시보드
├── backend/        # Spring Boot REST API
├── ai/             # 합성 데이터, 학습, 평가, 효과 분석
├── data/           # 생성 데이터와 분석 결과
├── database/       # PostgreSQL 스키마, 시드, 마이그레이션
└── docs/           # 설계, API, DB, AI 방법론, 협업 규칙
```

## 빠른 시작

### 1. 저장소 준비

```bash
git clone <repository-url>
cd runmile
cp .env.example .env
```

`.env`와 `frontend/.env.local`, `backend/.env`에는 실제 비밀값을 넣을 수 있지만 Git에는 커밋하지 않습니다.

### 2. PostgreSQL 실행

```bash
docker compose up -d
docker compose ps
```

새 볼륨에서는 `database/init.sql`과 `database/seed.sql`이 자동 실행됩니다. 기존 볼륨에 AI 평가 테이블이 없다면 다음 마이그레이션을 한 번 실행합니다.

```bash
docker compose exec -T postgres psql -U runmile -d runmile \
  < database/migrations/001_ai_evaluation.sql
docker compose exec -T postgres psql -U runmile -d runmile \
  < database/migrations/002_replace_mock_blockchain_records.sql
```

### 3. AI 배치 실행

macOS에서는 XGBoost용 OpenMP가 필요합니다.

```bash
brew install libomp
cd ai
/opt/homebrew/bin/python3.12 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python main.py
cd ..
```

배치는 합성 데이터, 정상매출 예측, 정책 효과, 평가 지표를 생성하고 PostgreSQL에 UPSERT합니다. 평가용 ground truth는 `data/results/ground_truth.csv`에만 저장하며 모델 입력이나 운영 DB에는 넣지 않습니다.

### 4. Backend 실행

```bash
cd backend
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
cp ../.env.example .env
set -a
source .env
set +a
unset DAEGU_CHAIN_PRIVATE_KEY
./gradlew bootRun
```

프로젝트 공개 지갑 주소와 Polygon Amoy 조회 설정은 안전한 기본값이 있어 별도 비밀값 없이 실행할 수 있습니다. API 주소는 `http://localhost:8080/api/v1`입니다.

### 5. Frontend 실행

`frontend/.env.local`을 만들고 Kakao Maps JavaScript 키와 Backend 주소를 입력합니다.

```dotenv
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_KAKAO_MAP_API_KEY=카카오맵_JavaScript_키
```

카카오 개발자 콘솔의 카카오맵 제품 설정과 Web 플랫폼 도메인도 등록해야 합니다. 로컬 주소는 일반적으로 `http://localhost:3000`입니다.

```bash
cd frontend
npm install
npm run dev
```

- 참가자 화면: `http://localhost:3000/participant`
- 관리자 화면: `http://localhost:3000/admin`

## 블록체인 완주 증명

### 구현 방식

완주 정보로 만든 SHA-256 해시를 Polygon Amoy의 `0 POL` 자기 전송 트랜잭션 input에 기록합니다. 초기 시드의 완주 증명은 `PENDING` 상태이며, 앵커링이 끝나기 전에는 RunMile 지급 자격으로 인정하지 않습니다.

온체인 검증 시 다음 항목을 모두 확인합니다.

- RPC 체인 ID가 Polygon Amoy의 `80002`인지
- 거래가 채굴에 성공했는지
- 저장된 거래 해시와 조회 결과가 같은지
- 송신자와 수신자가 설정된 신뢰 지갑 주소인지
- 전송 금액이 `0 POL`인지
- 거래 input이 참가자와 완주 기록으로 다시 만든 해시와 같은지
- 설정한 최소 확인 블록 수를 충족했는지

이 방식은 실제 테스트넷 거래를 사용하지만 ERC-721 NFT나 스마트 컨트랙트를 발행하지 않습니다. 기존 `/nft` API와 `nft_record` 명칭은 초기 MVP 계약과의 호환 때문에 유지합니다.

### 환경 변수

`backend/.env`를 만들고 다음 값을 설정합니다.

```dotenv
export DAEGU_CHAIN_RPC_URL=https://polygon-amoy.drpc.org
export DAEGU_CHAIN_NETWORK_NAME=POLYGON_AMOY
export DAEGU_CHAIN_ID=80002
export DAEGU_CHAIN_MINIMUM_CONFIRMATIONS=1
export DAEGU_CHAIN_RPC_TIMEOUT_SECONDS=10
export DAEGU_CHAIN_VERIFICATION_CACHE_SECONDS=30
export DAEGU_CHAIN_VERIFICATION_CACHE_MAX_SIZE=1000
export DAEGU_CHAIN_TRUSTED_ADDRESS=0xe957f0ad734260f9677b4d53e8d42c0c86556367
export DAEGU_CHAIN_PRIVATE_KEY=로컬_서명용_개인키
export DAEGU_CHAIN_ANCHOR_RUNNER_ID=1
export DAEGU_CHAIN_ANCHOR_ALL=false
export DAEGU_CHAIN_RECOVERY_TX_HASH=
```

공개 지갑 주소는 공유할 수 있지만 개인키는 비밀번호와 같습니다. 개인키는 로컬 앵커링 작업에서만 사용하고 GitHub, Render, Frontend 환경 변수, 로그에 넣지 않습니다. 권장 파일 권한은 다음과 같습니다.

```bash
chmod 600 backend/.env
```

### Amoy 테스트 POL 받기

앵커링 수수료를 내려면 테스트 POL이 필요합니다.

1. [Polygon Faucet](https://faucet.polygon.technology/)을 엽니다.
2. 네트워크는 `Amoy`, 토큰은 `POL`을 선택합니다.
3. `DAEGU_CHAIN_TRUSTED_ADDRESS`의 공개 주소를 입력합니다.
4. 지급 후 Amoy Polygonscan에서 잔액을 확인합니다.

테스트 POL은 실제 자산이 아닙니다. 이 지갑으로 실제 POL이나 다른 자산을 보내지 않습니다.

### 완주 기록 앵커링

특정 참가자 하나를 먼저 처리하는 것을 권장합니다.

```bash
cd backend
set -a
source .env
set +a
./gradlew bootRun --args='--spring.profiles.active=anchor-demo'
```

작업 순서는 체인 ID와 지갑 확인, 잔액 확인, 거래 전송, 영수증과 payload 재검증, 확인 블록 대기, DB 저장 순입니다. 성공하면 거래 해시가 `nft_record.nft_token_id`에, `POLYGON_AMOY`가 `network`에 저장됩니다.

모든 기록을 처리해야 할 때만 `DAEGU_CHAIN_ANCHOR_RUNNER_ID`를 비우고 `DAEGU_CHAIN_ANCHOR_ALL=true`를 명시합니다. 둘을 모두 지정하거나 모두 비우면 안전을 위해 실행이 중단됩니다.

### DB 저장 실패 복구

체인 거래는 성공했지만 DB 저장만 실패했다면 새 거래를 보내지 않습니다. 로그나 Amoy Polygonscan에서 기존 거래 해시를 확인한 뒤 다음 두 값을 설정하고 같은 명령을 다시 실행합니다.

```dotenv
export DAEGU_CHAIN_ANCHOR_RUNNER_ID=복구할_참가자_ID
export DAEGU_CHAIN_RECOVERY_TX_HASH=0x로_시작하는_기존_거래_해시
```

복구 모드는 거래의 송신자, 수신자, 금액, 완주 payload와 확인 블록을 다시 검증한 후 DB만 갱신합니다. 새 거래를 서명하지 않으므로 개인키가 필요하지 않습니다. 복구가 끝나면 `DAEGU_CHAIN_RECOVERY_TX_HASH`를 다시 비웁니다.

### 실제 검증 모드로 Backend 실행

일반 Backend는 거래를 서명하지 않으므로 개인키가 필요하지 않습니다.

```bash
cd backend
set -a
source .env
set +a
unset DAEGU_CHAIN_PRIVATE_KEY
unset DAEGU_CHAIN_ANCHOR_RUNNER_ID
unset DAEGU_CHAIN_ANCHOR_ALL
unset DAEGU_CHAIN_RECOVERY_TX_HASH
./gradlew bootRun
```

`GET /api/v1/runners/{runnerId}/nft`는 DB 값만 신뢰하지 않고 Polygon Amoy에서 거래를 다시 조회합니다. RPC 장애는 `503 BLOCKCHAIN_UNAVAILABLE`로 반환합니다.
동일한 완주 증명에 대한 반복 요청은 크기와 유효시간이 제한된 캐시를 사용해 공개 RPC 호출 폭주를 방지합니다.

배포 환경에는 다음 값만 설정합니다.

- `DAEGU_CHAIN_RPC_URL`
- `DAEGU_CHAIN_NETWORK_NAME`
- `DAEGU_CHAIN_ID`
- `DAEGU_CHAIN_MINIMUM_CONFIRMATIONS`
- `DAEGU_CHAIN_RPC_TIMEOUT_SECONDS`
- `DAEGU_CHAIN_VERIFICATION_CACHE_SECONDS`
- `DAEGU_CHAIN_VERIFICATION_CACHE_MAX_SIZE`
- `DAEGU_CHAIN_TRUSTED_ADDRESS`

`DAEGU_CHAIN_PRIVATE_KEY`는 배포 환경에 등록하지 않습니다.

## 주요 API

| 기능 | Method | 경로 |
|---|---|---|
| 참가자 조회 | GET | `/api/v1/runners/{runnerId}` |
| 완주 조회 | GET | `/api/v1/runners/{runnerId}/completion` |
| 완주 증명 조회 | GET | `/api/v1/runners/{runnerId}/nft` |
| RunMile 지급 | POST | `/api/v1/runners/{runnerId}/runmile/issue` |
| 지갑 조회 | GET | `/api/v1/runners/{runnerId}/wallet` |
| 거래 내역 | GET | `/api/v1/runners/{runnerId}/runmile/transactions` |
| 가맹점 검색 | GET | `/api/v1/merchants` |
| 결제 | POST | `/api/v1/payments` |
| 관리자 분석 | GET | `/api/v1/admin/analytics/*` |

성공 응답은 공통 wrapper 없이 직접 JSON을 반환합니다. 오류 응답은 다음 형식입니다.

```json
{
  "code": "ERROR_CODE",
  "message": "오류 설명"
}
```

자세한 계약은 [API 명세](docs/api-spec.md)를 확인합니다.

## 검증 명령

```bash
# Backend 전체 테스트와 실행 JAR 생성
cd backend
./gradlew clean test bootJar

# Frontend 빌드
cd ../frontend
npm run build

# AI 테스트
cd ../ai
source .venv/bin/activate
pytest
```

## 협업 규칙

작업은 작은 Issue 단위로 나누고, 보통 하나의 Issue를 하나의 브랜치와 PR로 완료합니다.

```text
브랜치: <type>/<area>/<issue-number>-<short-title>
커밋:   <type>(<area>): <한글 요약> (#<issue-number>)
PR:     <type>(<area>): <한글 요약> (#<issue-number>)
```

예시:

```text
feat/be/12-wallet-issue
fix/fe/18-map-marker
docs/common/21-api-spec
```

브랜치 이름에는 `#`을 넣지 않습니다. 상세 규칙은 [개발 협업 규칙](docs/development-workflow.md)을 따릅니다.

## 설계 문서

- [시스템 아키텍처](docs/architecture.md)
- [REST API 명세](docs/api-spec.md)
- [데이터베이스 설계](docs/database.md)
- [AI 분석 방법론](docs/ai-methodology.md)
- [개발 협업 규칙](docs/development-workflow.md)

## 시연 시 지켜야 할 표현

- 개인정보는 저장하지 않고 데모 식별자와 집계 데이터만 사용합니다.
- 대구로페이 결제 연동이 완료됐다고 표현하지 않습니다.
- Polygon Amoy 완주 증명은 실제 테스트넷 거래이지만 공식 대구체인 연동, NFT 발행, 스마트 컨트랙트 배포가 아닙니다.
- AI는 실시간 API가 아니라 배치 분석 후 PostgreSQL에 결과를 저장합니다.
- RunMile은 목적이 제한된 지역 소비 인센티브이며 암호화폐나 새로운 선불 지급수단이 아닙니다.
