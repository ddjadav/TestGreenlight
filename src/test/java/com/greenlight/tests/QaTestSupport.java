package com.greenlight.tests;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class QaTestSupport {
    private final Page page;
    private final String device;

    public QaTestSupport(Page page, String device) {
        this.page = page;
        this.device = device;
    }

    public TestResultData newResult(String url, String scenario) {
        TestResultData result = new TestResultData();
        result.url = url;
        result.device = device;
        result.scenario = scenario;
        return result;
    }

    public void attachListeners(TestResultData result) {
        page.onConsoleMessage(msg -> {
            if ("error".equalsIgnoreCase(msg.type())) {
                result.consoleErrors.add(msg.text());
            }
        });
        page.onRequestFailed(request -> result.networkFailures.add(request.method() + " " + request.url() + " failed"));
    }

    public void finalizeResult(TestResultData result, Response response, String screenshotName) throws IOException {
        result.httpStatus = response == null ? null : response.status();
        result.title = page.title();
        result.textExcerpt = excerpt(safeBodyText());
        result.blockedByBotProtection = isBotVerificationPage(result);
        result.blockReason = result.blockedByBotProtection
            ? "Bot verification page detected instead of the expected page."
            : null;

        Path screenshotDir = Path.of("target", "reports", device);
        Files.createDirectories(screenshotDir);
        result.screenshotPath = screenshotDir.resolve(screenshotName).toString().replace("\\", "/");
        page.screenshot(new Page.ScreenshotOptions().setPath(Path.of(result.screenshotPath)).setFullPage(true));
    }

    public void addCheck(TestResultData result, String name, boolean passed, String details) {
        result.checks.add(new TestResultData.CheckResult(name, passed, details));
    }

    public boolean hasVisibleText(String text) {
        try {
            Locator locator = page.getByText(text).first();
            return locator.isVisible();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public boolean hasVisibleSelector(String selector) {
        try {
            Locator locator = page.locator(selector).first();
            return locator.isVisible();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public boolean hasVisibleAnySelector(String... selectors) {
        for (String selector : selectors) {
            if (hasVisibleSelector(selector)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasVisibleRoleLink(String textPattern) {
        try {
            Locator locator = page.getByRole(com.microsoft.playwright.options.AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile(textPattern, Pattern.CASE_INSENSITIVE)));
            return locator.first().isVisible();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public boolean hasVisibleRoleButton(String textPattern) {
        try {
            Locator locator = page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile(textPattern, Pattern.CASE_INSENSITIVE)));
            return locator.first().isVisible();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public Locator firstVisibleLocator(String... selectors) {
        for (String selector : selectors) {
            try {
                Locator locator = page.locator(selector).first();
                if (locator.isVisible()) {
                    return locator;
                }
            } catch (RuntimeException ignored) {
                // Try the next selector.
            }
        }
        return null;
    }

    public boolean clickFirstVisible(String... selectors) {
        Locator locator = firstVisibleLocator(selectors);
        if (locator == null) {
            return false;
        }

        locator.click();
        page.waitForLoadState();
        return true;
    }

    public boolean navigateToFirstVisibleTarget(String... selectors) {
        Locator locator = firstVisibleLocator(selectors);
        if (locator == null) {
            return false;
        }

        String href = extractHref(locator);
        if (href == null || href.isBlank() || href.startsWith("#")) {
            try {
                locator.click();
                page.waitForLoadState();
                return true;
            } catch (RuntimeException ignored) {
                return false;
            }
        }

        page.navigate(href);
        page.waitForLoadState();
        return true;
    }

    public boolean isProductRelatedUrl(String url) {
        return url.contains("/product/")
            || url.contains("/shop")
            || url.contains("/product-category/")
            || url.contains("/products/");
    }

    public void writeAndAssert(TestResultData result, String fileStem) throws IOException {
        TestReportWriter.writeReports(result, fileStem);

        if (result.blockedByBotProtection) {
            Assertions.fail("Blocked by bot verification before test flow could continue.");
        }

        for (TestResultData.CheckResult check : result.checks) {
            Assertions.assertTrue(check.passed, check.name + " failed: " + check.details);
        }
    }

    private String safeBodyText() {
        try {
            return page.locator("body").innerText();
        } catch (RuntimeException ignored) {
            return "";
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

    private String extractHref(Locator locator) {
        try {
            String href = locator.getAttribute("href");
            if (href != null && !href.isBlank()) {
                return href;
            }
        } catch (RuntimeException ignored) {
            // Fall through to closest anchor lookup.
        }

        try {
            Object value = locator.evaluate(
                "node => node.closest('a') ? node.closest('a').href : null"
            );
            return value == null ? null : value.toString();
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}
