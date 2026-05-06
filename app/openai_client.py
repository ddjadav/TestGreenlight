from __future__ import annotations

import base64
import json
from pathlib import Path

from openai import OpenAI


PROMPT = """
You are a senior QA engineer reviewing a website screenshot and page context.

Find likely UI and UX problems, including:
- overlapping elements
- missing call-to-action buttons
- alignment issues
- broken visual hierarchy
- unreadable or low-contrast text
- inconsistent spacing
- suspicious empty sections
- mobile/desktop responsiveness issues

Also call out any signs that functionality may be broken based on the visible state.

Return strict JSON with this shape:
{
  "summary": "short summary",
  "issues": [
    {
      "severity": "low|medium|high",
      "title": "issue title",
      "details": "short explanation"
    }
  ]
}
"""


def _to_data_url(image_path: Path) -> str:
    encoded = base64.b64encode(image_path.read_bytes()).decode("utf-8")
    return f"data:image/png;base64,{encoded}"


def review_ui(
    api_key: str,
    model: str,
    image_path: Path,
    page_title: str,
    page_url: str,
    page_text_excerpt: str,
) -> dict:
    client = OpenAI(api_key=api_key)
    image_data_url = _to_data_url(image_path)

    response = client.responses.create(
        model=model,
        input=[
            {
                "role": "system",
                "content": [{"type": "input_text", "text": PROMPT.strip()}],
            },
            {
                "role": "user",
                "content": [
                    {
                        "type": "input_text",
                        "text": (
                            f"Page URL: {page_url}\n"
                            f"Page title: {page_title}\n"
                            f"Visible text excerpt:\n{page_text_excerpt[:4000]}"
                        ),
                    },
                    {
                        "type": "input_image",
                        "image_url": image_data_url,
                    },
                ],
            },
        ],
    )

    raw_text = response.output_text.strip()
    try:
        parsed = json.loads(raw_text)
        return {"raw_response": raw_text, "parsed": parsed}
    except json.JSONDecodeError:
        return {
            "raw_response": raw_text,
            "parsed": None,
            "warning": "Model response was not valid JSON",
        }
