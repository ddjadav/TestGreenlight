from __future__ import annotations

import json
from datetime import datetime
from pathlib import Path
from typing import Any


def timestamp_slug() -> str:
    return datetime.now().strftime("%Y%m%d_%H%M%S")


def ensure_output_dir(path: str) -> Path:
    output_path = Path(path)
    output_path.mkdir(parents=True, exist_ok=True)
    return output_path


def save_json_report(output_dir: Path, report: dict[str, Any], name: str) -> Path:
    report_path = output_dir / f"{name}.json"
    report_path.write_text(json.dumps(report, indent=2), encoding="utf-8")
    return report_path


def build_markdown_summary(report: dict[str, Any]) -> str:
    test_result = report["test_result"]
    ai_review = report["ai_review"]

    lines = [
        "# Website Test Summary",
        "",
        f"- URL: {test_result['url']}",
        f"- Device: {test_result['device']}",
        f"- Title: {test_result['title']}",
        "",
        "## Functional Checks",
    ]

    for check in test_result["checks"]:
        status = "PASS" if check["passed"] else "FAIL"
        lines.append(f"- {status}: {check['name']} ({check['details']})")

    lines.extend(
        [
            "",
            "## Console Errors",
        ]
    )
    if test_result["console_errors"]:
        lines.extend(f"- {item}" for item in test_result["console_errors"])
    else:
        lines.append("- None")

    lines.extend(
        [
            "",
            "## Network Failures",
        ]
    )
    if test_result["network_failures"]:
        lines.extend(f"- {item}" for item in test_result["network_failures"])
    else:
        lines.append("- None")

    lines.extend(
        [
            "",
            "## AI Review",
        ]
    )
    if ai_review.get("parsed"):
        parsed = ai_review["parsed"]
        lines.append(f"- Summary: {parsed.get('summary', 'No summary provided')}")
        issues = parsed.get("issues", [])
        if issues:
            for issue in issues:
                lines.append(
                    f"- {issue.get('severity', 'unknown').upper()}: "
                    f"{issue.get('title', 'Untitled')} - {issue.get('details', '')}"
                )
        else:
            lines.append("- No AI issues reported")
    elif ai_review.get("skipped"):
        lines.append(f"- Skipped: {ai_review.get('reason', 'No reason provided')}")
    else:
        lines.append("- AI output was not valid JSON")

    return "\n".join(lines) + "\n"


def save_markdown_summary(output_dir: Path, content: str, name: str) -> Path:
    summary_path = output_dir / f"{name}.md"
    summary_path.write_text(content, encoding="utf-8")
    return summary_path


def should_fail_run(report: dict[str, Any]) -> bool:
    test_result = report["test_result"]
    if any(not check["passed"] for check in test_result["checks"]):
        return True
    if test_result["console_errors"]:
        return True
    if test_result["network_failures"]:
        return True
    return False
