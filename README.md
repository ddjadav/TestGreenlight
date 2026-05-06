# TestGreenlight

A Python starter project for testing `qa.greenlightmedicare.co.uk` functionality and UI with:

- Playwright for browser automation
- OpenAI for AI-based UI review
- Rule-based checks for repeatable functionality testing

## What it does

The agent:

1. Opens a website in Playwright
2. Checks core functionality like page load and visible selectors
3. Captures a screenshot
4. Sends the screenshot plus page context to OpenAI for UI review
5. Saves a JSON report with findings

## Project structure

```text
TestGreenlight/
  app/
    __init__.py
    agent.py
    config.py
    openai_client.py
    report.py
    tester.py
  outputs/
  .env.example
  requirements.txt
  run.py
```

## Local setup

Create a virtual environment and install dependencies:

```powershell
cd C:\Users\DELL\Documents\TestGreenlight
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python -m playwright install
```

## Configure environment

Copy `.env.example` to `.env` and add your OpenAI API key:

```powershell
Copy-Item .env.example .env
```

Example `.env`:

```env
OPENAI_API_KEY=your_api_key_here
OPENAI_MODEL=gpt-5.4-mini
TEST_URL=https://qa.greenlightmedicare.co.uk
```

## Run locally

Basic example:

```powershell
python run.py
```

With custom checks:

```powershell
python run.py --expect-text "Login" --expect-selector "h1"
```

Mobile viewport:

```powershell
python run.py --device mobile
```

## Cloud setup with GitHub Actions

1. In GitHub repository settings, add these values:

- Repository secret: `OPENAI_API_KEY`
- Repository variable: `GREENLIGHT_EXPECT_TEXT` with a stable visible string from the QA homepage
- Repository variable: `GREENLIGHT_EXPECT_SELECTOR` with a stable selector like `header`, `main`, or a login button selector

2. Run the `Website QA Test` workflow from the Actions tab.

The workflow also runs daily on GitHub-hosted runners.

## Output

Results are saved in `outputs/`:

- screenshot PNG
- JSON report
- Markdown summary
- workflow artifacts for desktop and mobile runs

## Emailing failure reports

This repo is ready to produce artifacts in GitHub Actions. To auto-email failures, add one of these next:

- Gmail connector in this chat so I can help route reports
- SMTP secrets in GitHub Actions for automated mail delivery

## Good next improvements

- Add login flow support
- Add multi-page crawl
- Add form submission checks
- Add accessibility checks with `axe-core`
- Add pytest test suite for CI
