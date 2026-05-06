from __future__ import annotations

import argparse
import os
from dataclasses import dataclass

from dotenv import load_dotenv


@dataclass
class AppConfig:
    url: str
    expect_text: str | None
    expect_selector: str | None
    device: str
    output_dir: str
    openai_api_key: str | None
    openai_model: str


def load_config() -> AppConfig:
    load_dotenv()

    parser = argparse.ArgumentParser(description="AI website tester")
    parser.add_argument(
        "--url",
        default=os.getenv("TEST_URL", "https://qa.greenlightmedicare.co.uk"),
        help="Website URL to test",
    )
    parser.add_argument(
        "--expect-text",
        default=os.getenv("EXPECT_TEXT"),
        help="Text expected to appear on the page",
    )
    parser.add_argument(
        "--expect-selector",
        default=os.getenv("EXPECT_SELECTOR"),
        help="CSS selector expected to be visible",
    )
    parser.add_argument(
        "--device",
        default="desktop",
        choices=["desktop", "mobile"],
        help="Viewport preset",
    )
    parser.add_argument(
        "--output-dir",
        default="outputs",
        help="Directory where screenshots and reports are saved",
    )
    args = parser.parse_args()

    return AppConfig(
        url=args.url,
        expect_text=args.expect_text,
        expect_selector=args.expect_selector,
        device=args.device,
        output_dir=args.output_dir,
        openai_api_key=os.getenv("OPENAI_API_KEY"),
        openai_model=os.getenv("OPENAI_MODEL", "gpt-5.4-mini"),
    )
