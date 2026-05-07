package com.greenlight.tests;

import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class PrimaryNavigationSmokeTest extends BaseTest {

    @Test
    void headerNavigationAndKeyEntryPointsArePresent() throws IOException {
        QaTestSupport support = new QaTestSupport(page, device);
        TestResultData result = support.newResult(baseUrl, "primary-navigation-smoke");
        support.attachListeners(result);

        Response response = page.navigate(baseUrl);
        page.waitForLoadState();

        support.finalizeResult(result, response, "primary-navigation-smoke.png");
        support.addCheck(result, "page_loaded", response != null && response.ok(),
            response == null ? "No response" : "HTTP status: " + response.status());
        support.addCheck(result, "header_or_nav_visible",
            support.hasVisibleSelector("header") || support.hasVisibleSelector("nav"),
            "Expected header or nav to be visible");
        support.addCheck(result, "shop_link_or_category_text_present",
            support.hasVisibleRoleLink("shop|products|categories") || support.hasVisibleText("Shop by categories"),
            "Expected a shop/categories entry point");
        support.addCheck(result, "cart_or_checkout_entry_present",
            support.hasVisibleRoleLink("cart|basket|checkout") || support.hasVisibleRoleButton("cart|basket|checkout"),
            "Expected a cart or checkout entry point");

        support.writeAndAssert(result, "primary-navigation-smoke");
    }
}
