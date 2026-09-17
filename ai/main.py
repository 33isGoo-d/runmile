from common.config import RESULTS_DIR, SYNTHETIC_DIR
from effect.export_policy_effects import export_policy_effects
from evaluation.evaluate_effect import evaluate_effect
from generator.synthetic_generator import generate_all
from training.train_baseline import train_baseline


def main() -> None:
    data = generate_all()

    SYNTHETIC_DIR.mkdir(parents=True, exist_ok=True)
    data["merchants"].to_csv(SYNTHETIC_DIR / "merchants.csv", index=False)
    data["merchant_daily_sales"].to_csv(SYNTHETIC_DIR / "merchant_daily_sales.csv", index=False)

    RESULTS_DIR.mkdir(parents=True, exist_ok=True)
    data["ground_truth"].to_csv(RESULTS_DIR / "ground_truth.csv", index=False)
    print(
        f"Generated {len(data['merchants'])} merchants, "
        f"{len(data['merchant_daily_sales'])} daily sales rows"
    )

    train_baseline()
    export_policy_effects()
    evaluate_effect()


if __name__ == "__main__":
    main()
