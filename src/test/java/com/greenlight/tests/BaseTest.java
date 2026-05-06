package com.greenlight.tests;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class BaseTest {
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;
    protected String baseUrl;
    protected String expectedText;
    protected String expectedSelector;
    protected String device;

    @BeforeEach
    void setUp() {
        Properties properties = loadProperties();

        baseUrl = getConfig("base.url", properties, "https://qa.greenlightmedicare.co.uk");
        expectedText = getConfig("expected.text", properties, "Shop by categories");
        expectedSelector = getConfig("expected.selector", properties, "main");
        device = getConfig("device", properties, "desktop");
        boolean headless = Boolean.parseBoolean(getConfig("headless", properties, "true"));

        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
        context = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
        page = context.newPage();
        page.setDefaultTimeout(20_000);

        if ("mobile".equalsIgnoreCase(device)) {
            page.setViewportSize(390, 844);
        } else {
            page.setViewportSize(1440, 900);
        }
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    protected String getConfig(String key, Properties properties, String defaultValue) {
        String fromSystem = System.getProperty(key);
        if (fromSystem != null && !fromSystem.isBlank()) {
            return fromSystem;
        }
        return properties.getProperty(key, defaultValue);
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("test-config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException ignored) {
            // Defaults are used when the config file is unavailable.
        }
        return properties;
    }
}
