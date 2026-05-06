package com.greenlight.tests;

import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HomePageQaTest extends BaseTest {

    @Test
    void homepageLoadsOrReportsBotBlockClearly() throws IOException {
        TestResultData result = new TestResultData();
        result.url = baseUrl;
        result.device = device;

        page.onConsoleMessage(msg -> {
            if (msg.type() == ConsoleMessage.Type.ERROR) {
                result.consoleErrors.add(msg.text());
            }
        });
        page.onRequestFailed(request -> result.networkFailures.add(request.method() + " " + request.url() + " failed"));

        Response response = page.navigate(baseUrl);
        page.waitForLoadState();

        result.httpStatus = response == null ? null : response.status();
        result.title = page.title();
        result.textExcerpt = excerpt(page.locator("body").innerText());
        result.blockedByBotProtection = isBotVerificationPage(result);
        result.blockReason = result.blockedByBotProtection
            ? "Bot verification page detected instead of the expected homepage."
            : null;

        Path screenshotDir = Path.of("target", "reports", device);
        Files.createDirectories(screenshotDir);
        result.screenshotPath = screenshotDir.resolve("latest-screenshot.png").toString().replace("\\", "/");
        page.screenshot(new Page.ScreenshotOptions().setPath(Path.of(result.screenshotPath)).setFullPage(true));

        addCheck(result, "page_loaded", response != null && response.ok(),
            response == null ? "No response" : "HTTP status: " + response.status());
        addCheck(result, "expected_text_visible", hasVisibleText(expectedText),
            "Expected text: " + expectedText);
        addCheck(result, "expected_selector_visible", hasVisibleSelector(expectedSelector),
            "Expected selector: " + expectedSelector);

        TestReportWriter.writeReports(result, "report");

        if (result.blockedByBotProtection) {
            Assertions.fail("Blocked by bot verification before homepage content loaded.");
        }

        for (TestResultData.CheckResult check : result.checks) {
            Assertions.assertTrue(check.passed, check.name + " failed: " + check.details);
        }
    }

    private void addCheck(TestResultData result, String name, boolean passed, String details) {
        result.checks.add(new TestResultData.CheckResult(name, passed, details));
    }

    private boolean hasVisibleText(String text) {
        try {
            Locator locator = page.getByText(text).first();
            return locator.isVisible();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private boolean hasVisibleSelector(String selector) {
        try {
            Locator locator = page.locator(selector).first();
            return locator.isVisible();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private boolean isBotVerificationPage(TestResultData result) {
        String title = result.title == null ? "" : result.title.toLowerCase();
        String text = result.textExcerpt == null ? "" : result.textExcerpt.toLowerCase();
        return title.contains("bot verification")
            || text.contains("verifying that you are not a robot")
            || text.contains("captcha")
            || text.contains("recaptcha");
    }

    private String excerpt(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 5000 ? text : text.substring(0, 5000);
    }
}
