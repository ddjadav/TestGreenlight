from __future__ import annotations

import json
import sys
from pathlib import Path

from app.config import load_config
from app.openai_client import review_ui
from app.report import (
    build_markdown_summary,
    ensure_output_dir,
    save_json_report,
    save_markdown_summary,
    should_fail_run,
    timestamp_slug,
)
from app.tester import run_website_checks


def main() -> None:
    config = load_config()
    output_dir = ensure_output_dir(config.output_dir)

    test_result = run_website_checks(
        url=config.url,
        expect_text=config.expect_text,
        expect_selector=config.expect_selector,
        device=config.device,
        output_dir=output_dir,
    )

    ai_review = {
        "skipped": True,
        "reason": "OPENAI_API_KEY not set",
    }

    if config.openai_api_key:
        ai_review = review_ui(
            api_key=config.openai_api_key,
            model=config.openai_model,
            image_path=Path(test_result["screenshot_path"]),
            page_title=test_result["title"],
            page_url=test_result["url"],
            page_text_excerpt=test_result["text_excerpt"],
        )

    report_name = f"report_{timestamp_slug()}"
    report = {
        "test_result": test_result,
        "ai_review": ai_review,
    }
    report_path = save_json_report(output_dir, report, report_name)
    summary = build_markdown_summary(report)
    summary_path = save_markdown_summary(output_dir, summary, report_name)

    print(json.dumps(report, indent=2))
    print(f"\nSaved report to: {report_path}")
    print(f"Saved summary to: {summary_path}")

    if should_fail_run(report):
        sys.exit(1)
