from pathlib import Path
import os

AI_DIR = Path(__file__).resolve().parent.parent
DATA_DIR = AI_DIR.parent / "data"
SYNTHETIC_DIR = DATA_DIR / "synthetic"
PROCESSED_DIR = DATA_DIR / "processed"
RESULTS_DIR = DATA_DIR / "results"
MODELS_DIR = AI_DIR / "models"

POSTGRES_HOST = os.getenv("POSTGRES_HOST", "localhost")
POSTGRES_PORT = int(os.getenv("POSTGRES_PORT", "5432"))
POSTGRES_DB = os.getenv("POSTGRES_DB", "runmile")
POSTGRES_USER = os.getenv("POSTGRES_USER", "runmile")
POSTGRES_PASSWORD = os.getenv("POSTGRES_PASSWORD", "runmile_dev_password")
SCENARIOS = ("NONE", "LOW", "MEDIUM", "HIGH")
MERCHANT_CATEGORIES = ("RESTAURANT", "CAFE", "RETAIL", "ACCOMMODATION", "OTHER")
DISTRICTS = ("중구", "동구", "서구", "남구", "북구", "수성구", "달서구")

# Fixed category codes for model features so encoding stays identical across
# training, validation, and prediction (pandas' .cat.codes reorders per-subset).
DISTRICT_CODE = {district: i for i, district in enumerate(DISTRICTS)}
CATEGORY_CODE = {category: i for i, category in enumerate(MERCHANT_CATEGORIES)}

# database/init.sql seeds the demo completion on this date; keep the synthetic
# marathon day consistent with it so seed data and generated data agree.
MARATHON_DATE = "2026-02-22"
PRE_EVENT_WEEKS = 8
RANDOM_SEED = 42

MERCHANTS_PER_DISTRICT_CATEGORY = 10
TREATMENT_RATIO = 0.6

# Simulation parameters for validation, not real-world policy claims.
RUNMILE_EFFECT_RANGE = {
    "NONE": (0.0, 0.0),
    "LOW": (0.03, 0.08),
    "MEDIUM": (0.10, 0.20),
    "HIGH": (0.20, 0.35),
}

# Total RunMile issued to finishers is a fixed simulation input; district/category
# rows split this proportionally to where it was actually used (see export_policy_effects).
RUNMILE_BUDGET_BY_SCENARIO = {
    "NONE": 25_000_000,
    "LOW": 25_000_000,
    "MEDIUM": 25_000_000,
    "HIGH": 25_000_000,
}
