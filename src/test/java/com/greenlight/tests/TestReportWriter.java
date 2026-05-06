package com.greenlight.tests;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestReportWriter {
    private TestReportWriter() {
    }

    public static void writeReports(TestResultData result, String fileStem) throws IOException {
        Path outputDir = Path.of("target", "reports", result.device);
        Files.createDirectories(outputDir);

        Files.writeString(outputDir.resolve(fileStem + ".json"), toJson(result), StandardCharsets.UTF_8);
        Files.writeString(outputDir.resolve(fileStem + ".md"), toMarkdown(result), StandardCharsets.UTF_8);
    }

    private static String toMarkdown(TestResultData result) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Website Test Summary\n\n");
        sb.append("- URL: ").append(result.url).append("\n");
        sb.append("- Device: ").append(result.device).append("\n");
        sb.append("- Title: ").append(result.title).append("\n");
        sb.append("- HTTP status: ").append(result.httpStatus == null ? "unknown" : result.httpStatus).append("\n");
        sb.append("- Blocked by bot protection: ").append(result.blockedByBotProtection).append("\n");
        if (result.blockReason != null && !result.blockReason.isBlank()) {
            sb.append("- Block reason: ").append(result.blockReason).append("\n");
        }

        sb.append("\n## Functional Checks\n");
        for (TestResultData.CheckResult check : result.checks) {
            sb.append("- ").append(check.passed ? "PASS" : "FAIL")
                .append(": ").append(check.name)
                .append(" (").append(check.details).append(")\n");
        }

        sb.append("\n## Console Errors\n");
        if (result.consoleErrors.isEmpty()) {
            sb.append("- None\n");
        } else {
            for (String error : result.consoleErrors) {
                sb.append("- ").append(error).append("\n");
            }
        }

        sb.append("\n## Network Failures\n");
        if (result.networkFailures.isEmpty()) {
            sb.append("- None\n");
        } else {
            for (String failure : result.networkFailures) {
                sb.append("- ").append(failure).append("\n");
            }
        }

        return sb.toString();
    }

    private static String toJson(TestResultData result) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        appendJsonField(sb, 1, "title", result.title, true);
        appendJsonField(sb, 1, "url", result.url, true);
        appendJsonField(sb, 1, "device", result.device, true);
        appendJsonField(sb, 1, "screenshotPath", result.screenshotPath, true);
        appendJsonField(sb, 1, "textExcerpt", result.textExcerpt, true);
        appendJsonNumberField(sb, 1, "httpStatus", result.httpStatus, true);
        appendJsonBooleanField(sb, 1, "blockedByBotProtection", result.blockedByBotProtection, true);
        appendJsonField(sb, 1, "blockReason", result.blockReason, true);
        appendChecks(sb, result);
        appendJsonStringArray(sb, 1, "consoleErrors", result.consoleErrors, true);
        appendJsonStringArray(sb, 1, "networkFailures", result.networkFailures, false);
        sb.append("}\n");
        return sb.toString();
    }

    private static void appendChecks(StringBuilder sb, TestResultData result) {
        indent(sb, 1).append("\"checks\": [");
        if (!result.checks.isEmpty()) {
            sb.append("\n");
            for (int i = 0; i < result.checks.size(); i++) {
                TestResultData.CheckResult check = result.checks.get(i);
                indent(sb, 2).append("{\n");
                appendJsonField(sb, 3, "name", check.name, true);
                appendJsonBooleanField(sb, 3, "passed", check.passed, true);
                appendJsonField(sb, 3, "details", check.details, false);
                indent(sb, 2).append("}");
                if (i < result.checks.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            indent(sb, 1).append("]");
        } else {
            sb.append("]");
        }
        sb.append(",\n");
    }

    private static void appendJsonStringArray(StringBuilder sb, int indentLevel, String name, java.util.List<String> values, boolean trailingComma) {
        indent(sb, indentLevel).append("\"").append(name).append("\": [");
        if (!values.isEmpty()) {
            sb.append("\n");
            for (int i = 0; i < values.size(); i++) {
                indent(sb, indentLevel + 1).append("\"").append(escape(values.get(i))).append("\"");
                if (i < values.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            indent(sb, indentLevel).append("]");
        } else {
            sb.append("]");
        }
        if (trailingComma) {
            sb.append(",");
        }
        sb.append("\n");
    }

    private static void appendJsonField(StringBuilder sb, int indentLevel, String name, String value, boolean trailingComma) {
        indent(sb, indentLevel).append("\"").append(name).append("\": ");
        if (value == null) {
            sb.append("null");
        } else {
            sb.append("\"").append(escape(value)).append("\"");
        }
        if (trailingComma) {
            sb.append(",");
        }
        sb.append("\n");
    }

    private static void appendJsonNumberField(StringBuilder sb, int indentLevel, String name, Integer value, boolean trailingComma) {
        indent(sb, indentLevel).append("\"").append(name).append("\": ");
        sb.append(value == null ? "null" : value);
        if (trailingComma) {
            sb.append(",");
        }
        sb.append("\n");
    }

    private static void appendJsonBooleanField(StringBuilder sb, int indentLevel, String name, boolean value, boolean trailingComma) {
        indent(sb, indentLevel).append("\"").append(name).append("\": ").append(value);
        if (trailingComma) {
            sb.append(",");
        }
        sb.append("\n");
    }

    private static StringBuilder indent(StringBuilder sb, int indentLevel) {
        for (int i = 0; i < indentLevel; i++) {
            sb.append("  ");
        }
        return sb;
    }

    private static String escape(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n");
    }
}
