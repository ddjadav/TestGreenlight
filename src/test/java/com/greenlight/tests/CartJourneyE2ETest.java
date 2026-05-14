package com.greenlight.tests;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CartJourneyE2ETest extends BaseTest {

    @Test
    void userCanAttemptAddToCartAndReachCartOrCheckout() throws IOException {
        QaTestSupport support = new QaTestSupport(page, device);
        TestResultData result = support.newResult(baseUrl, "cart-journey-e2e");
        support.attachListeners(result);

        Response response = page.navigate(baseUrl);
        page.waitForLoadState();

        boolean reachedCatalog = false;
        boolean reachedProduct = false;
        boolean clickedCartAction = false;
        boolean reachedCartLikePage = false;

        reachedCatalog = support.navigateToFirstVisibleTarget(
            "a[href*='/shop/']",
            "a[href*='/products/']",
            "a[href*='/product-category/']",
            "nav a[href*='/shop']",
            "nav a[href*='/products/']",
            "nav a[href*='/product-category/']",
            "a:has-text('Products')",
            "a:has-text('Shop')",
            "a:has-text('Categories')"
        );

         boolean openedProductTarget = support.navigateToFirstVisibleTarget(
            "a[href*='/product/']",
            ".product a",
            ".woocommerce-loop-product__title"
        );
        if (openedProductTarget) {
            reachedProduct = page.url().contains("/product/");

            Locator cartAction = support.firstVisibleLocator(
                "button[name='add-to-cart']",
                "button.single_add_to_cart_button",
                "a.single_add_to_cart_button",
                "button:has-text('Add to basket')",
                "button:has-text('Add to cart')",
                "a:has-text('Add to basket')"
            );
            if (cartAction != null) {
                cartAction.click();
                page.waitForLoadState();
                clickedCartAction = true;
            }

            Locator cartLink = support.firstVisibleLocator(
                "a[href*='/cart']",
                "a[href*='/checkout']",
                "a:has-text('View cart')",
                "a:has-text('Checkout')"
            );
            if (cartLink != null) {
                cartLink.click();
                page.waitForLoadState();
            }

            reachedCartLikePage = page.url().contains("/cart") || page.url().contains("/checkout");
        }

        support.finalizeResult(result, response, "cart-journey-e2e.png");
        support.addCheck(result, "page_loaded", response != null && response.ok(),
            response == null ? "No response" : "HTTP status: " + response.status());
        support.addCheck(result, "reached_catalog_or_shop_page", reachedCatalog,
            "Expected to leave the homepage via a shop or category entry point");
        support.addCheck(result, "reached_product_page", reachedProduct,
            "Expected to open at least one product detail page");
        support.addCheck(result, "clicked_add_to_cart_or_equivalent", clickedCartAction,
            "Expected an add-to-cart style action on the product page");
        support.addCheck(result, "reached_cart_or_checkout", reachedCartLikePage,
            "Expected to reach cart or checkout but current URL is: " + page.url());

        support.writeAndAssert(result, "cart-journey-e2e");
    }
}
