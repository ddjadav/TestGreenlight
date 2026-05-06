package com.greenlight.tests;

import java.util.ArrayList;
import java.util.List;

public class TestResultData {
    public String title;
    public String url;
    public String device;
    public String screenshotPath;
    public String textExcerpt;
    public Integer httpStatus;
    public boolean blockedByBotProtection;
    public String blockReason;
    public final List<CheckResult> checks = new ArrayList<>();
    public final List<String> consoleErrors = new ArrayList<>();
    public final List<String> networkFailures = new ArrayList<>();

    public static class CheckResult {
        public final String name;
        public final boolean passed;
        public final String details;

        public CheckResult(String name, boolean passed, String details) {
            this.name = name;
            this.passed = passed;
            this.details = details;
        }
    }
}
