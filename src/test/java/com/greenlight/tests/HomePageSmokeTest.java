package com.greenlight.tests;

import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class HomePageSmokeTest extends BaseTest {

    @Test
    void homepageLoadsAndCoreElementsAreVisible() throws IOException {
        QaTestSupport support = new QaTestSupport(page, device);
        TestResultData result = support.newResult(baseUrl, "homepage-smoke");
        support.attachListeners(result);

        Response response = page.navigate(baseUrl);
        page.waitForLoadState();

        support.finalizeResult(result, response, "homepage-smoke.png");
        support.addCheck(result, "page_loaded", response != null && response.ok(),
            response == null ? "No response" : "HTTP status: " + response.status());
        support.addCheck(result, "expected_text_visible", support.hasVisibleText(expectedText),
            "Expected text: " + expectedText);
        support.addCheck(result, "expected_selector_visible", support.hasVisibleSelector(expectedSelector),
            "Expected selector: " + expectedSelector);
        support.addCheck(result, "title_not_blank", result.title != null && !result.title.isBlank(),
            "Page title should not be blank");

        support.writeAndAssert(result, "homepage-smoke");
    }
}
