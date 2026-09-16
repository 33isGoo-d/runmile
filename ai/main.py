from generator.synthetic_generator import build_synthetic_preview


def main() -> None:
    preview = build_synthetic_preview()
    print(f"RunMile AI batch skeleton ready: {len(preview)} preview rows")


if __name__ == "__main__":
    main()

