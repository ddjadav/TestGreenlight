# TestGreenlight

A Java Playwright project for testing `qa.greenlightmedicare.co.uk` functionality and UI with:

- Playwright for browser automation
- JUnit 5 for test execution
- GitHub Actions for free cloud runs

## What it does

The test suite:

1. Opens the QA site in Playwright
2. Checks page load, expected text, and expected selectors
3. Captures a screenshot
4. Detects bot-verification blocks explicitly
5. Saves JSON and Markdown reports under `target/reports/`

## Project structure

```text
TestGreenlight/
  src/test/java/com/greenlight/tests/
  src/test/resources/
  target/reports/
  pom.xml
```

## Local setup

Prerequisites:

- Java 17+
- Maven 3.9+

Install Playwright browsers:

```powershell
cd C:\Users\DELL\Documents\TestGreenlight
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
```

Run the desktop QA test:

```powershell
mvn test
```

Run the mobile QA test:

```powershell
mvn -Ddevice=mobile test
```

## Cloud setup with GitHub Actions

In GitHub repository settings, add these values:

- Repository variable: `GREENLIGHT_EXPECT_TEXT`
- Repository variable: `GREENLIGHT_EXPECT_SELECTOR`

Then run the `Website QA Test` workflow from the Actions tab.

The workflow also runs daily on GitHub-hosted runners and uploads `target/reports/` as artifacts.

## Output

Results are saved in `target/reports/<device>/`:

- screenshot PNG
- JSON report
- Markdown summary

## Current behavior

The Java test reports anti-bot blocks separately. If the site returns a bot-verification page, the report marks that explicitly instead of treating it as a generic homepage bug.

## Good next improvements

- Add login flow support
- Add multi-page crawl
- Add form submission checks
- Add accessibility checks
- Add SMTP email notifications from GitHub Actions
