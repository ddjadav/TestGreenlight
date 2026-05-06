from __future__ import annotations

from dataclasses import asdict, dataclass
from pathlib import Path

from playwright.sync_api import Error, Page, sync_playwright


@dataclass
class CheckResult:
    name: str
    passed: bool
    details: str


def _set_viewport(page: Page, device: str) -> None:
    if device == "mobile":
        page.set_viewport_size({"width": 390, "height": 844})
    else:
        page.set_viewport_size({"width": 1440, "height": 900})


def _is_text_visible(page: Page, text: str, timeout_ms: int = 5000) -> bool:
    locator = page.get_by_text(text).first
    try:
        locator.wait_for(state="visible", timeout=timeout_ms)
        return True
    except Error:
        return False


def _is_selector_visible(page: Page, selector: str, timeout_ms: int = 5000) -> bool:
    locator = page.locator(selector).first
    try:
        locator.wait_for(state="visible", timeout=timeout_ms)
        return True
    except Error:
        return False


def run_website_checks(
    url: str,
    expect_text: str | None,
    expect_selector: str | None,
    device: str,
    output_dir: Path,
) -> dict:
    console_errors: list[str] = []
    network_failures: list[str] = []
    checks: list[CheckResult] = []

    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=True)
        page = browser.new_page()
        _set_viewport(page, device)

        page.on(
            "console",
            lambda msg: console_errors.append(msg.text)
            if msg.type == "error"
            else None,
        )
        page.on(
            "requestfailed",
            lambda request: network_failures.append(
                f"{request.method} {request.url} failed"
            ),
        )

        response = page.goto(url, wait_until="networkidle", timeout=60000)

        checks.append(
            CheckResult(
                name="page_loaded",
                passed=response is not None and response.ok,
                details=(
                    f"HTTP status: {response.status}" if response is not None else "No response"
                ),
            )
        )

        if expect_text:
            visible = _is_text_visible(page, expect_text)
            checks.append(
                CheckResult(
                    name="expected_text_visible",
                    passed=visible,
                    details=f"Expected text: {expect_text}",
                )
            )

        if expect_selector:
            visible = _is_selector_visible(page, expect_selector)
            checks.append(
                CheckResult(
                    name="expected_selector_visible",
                    passed=visible,
                    details=f"Expected selector: {expect_selector}",
                )
            )

        title = page.title()
        text_excerpt = page.locator("body").inner_text(timeout=5000)[:5000]

        screenshot_path = output_dir / "latest_screenshot.png"
        page.screenshot(path=str(screenshot_path), full_page=True)

        browser.close()

    return {
        "title": title,
        "url": url,
        "device": device,
        "screenshot_path": str(screenshot_path),
        "text_excerpt": text_excerpt,
        "checks": [asdict(check) for check in checks],
        "console_errors": console_errors,
        "network_failures": network_failures,
    }
