package com.greenlight.tests;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ProductDiscoveryE2ETest extends BaseTest {

    @Test
    void userCanReachAProductOrCategoryFromTheHomepage() throws IOException {
        QaTestSupport support = new QaTestSupport(page, device);
        TestResultData result = support.newResult(baseUrl, "product-discovery-e2e");
        support.attachListeners(result);

        Response response = page.navigate(baseUrl);
        page.waitForLoadState();

        boolean clicked = false;
        Locator entryPoint = support.firstVisibleLocator(
            "a[href*='/product/']",
            "a[href*='/shop/']",
            "a[href*='/product-category/']",
            ".product a",
            ".woocommerce-loop-product__title"
        );
        if (entryPoint != null) {
            entryPoint.click();
            page.waitForLoadState();
            clicked = true;
        }

        support.finalizeResult(result, response, "product-discovery-e2e.png");
        support.addCheck(result, "page_loaded", response != null && response.ok(),
            response == null ? "No response" : "HTTP status: " + response.status());
        support.addCheck(result, "clicked_product_or_category_entry", clicked,
            "Expected at least one product or category entry point from the homepage");
        support.addCheck(result, "navigated_to_product_related_page",
            page.url().contains("/product/") || page.url().contains("/shop/") || page.url().contains("/product-category/"),
            "Expected navigation to a product, shop, or category page but was: " + page.url());
        support.addCheck(result, "product_page_has_actionable_content",
            support.hasVisibleRoleButton("add to basket|add to cart|select options")
                || support.hasVisibleText("Add to basket")
                || support.hasVisibleText("Select options"),
            "Expected product-related actionable content");

        support.writeAndAssert(result, "product-discovery-e2e");
    }
}
