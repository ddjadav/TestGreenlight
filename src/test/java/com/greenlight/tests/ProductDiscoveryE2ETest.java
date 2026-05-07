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

        boolean clickedHomeEntry = false;
        boolean reachedProductRelatedPage = false;
        boolean foundDiscoveryContent = false;

        Locator entryPoint = support.firstVisibleLocator(
            "a[href*='/product/']",
            "a[href*='/shop/']",
            "a[href*='/product-category/']",
            ".product a",
            ".woocommerce-loop-product__title",
            "nav a[href*='/shop']",
            "nav a[href*='/product-category/']",
            "a:has-text('Shop')",
            "a:has-text('Categories')"
        );
        if (entryPoint != null) {
            entryPoint.click();
            page.waitForLoadState();
            clickedHomeEntry = true;
            reachedProductRelatedPage = support.isProductRelatedUrl(page.url());
        }

        if (reachedProductRelatedPage) {
            foundDiscoveryContent = support.hasVisibleAnySelector(
                "a[href*='/product/']",
                ".product",
                ".products",
                ".woocommerce-loop-product__title",
                ".product-category"
            ) || support.hasVisibleRoleButton("add to basket|add to cart|select options")
                || support.hasVisibleText("Add to basket")
                || support.hasVisibleText("Select options");
        }

        support.finalizeResult(result, response, "product-discovery-e2e.png");
        support.addCheck(result, "page_loaded", response != null && response.ok(),
            response == null ? "No response" : "HTTP status: " + response.status());
        support.addCheck(result, "clicked_product_or_category_entry", clickedHomeEntry,
            "Expected a visible shop, category, or product entry point from the homepage");
        support.addCheck(result, "navigated_to_product_related_page", reachedProductRelatedPage,
            "Expected navigation to a product, shop, or category page but was: " + page.url());
        support.addCheck(result, "product_page_has_actionable_content", foundDiscoveryContent,
            "Expected a product grid, category listing, or product actions after navigation");

        support.writeAndAssert(result, "product-discovery-e2e");
    }
}
