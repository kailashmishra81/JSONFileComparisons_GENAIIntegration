Project: JSON Structural Comparator with GENAI Integration (Karate + Java)
=================================================

What this project does
----------------------
This repository contains a small test framework built on Karate + Java that:

- Compares JSON response files grouped by scenario (adjacent/pairwise comparisons).
- Produces deterministic structural diffs (added/removed fields) using a Java comparator.
- Optionally triggers an OpenAI analysis when structural differences exceed a threshold; the LLM produces a human-friendly summary.
- Writes two HTML reports after a run:
  - `overall-report.html`: all deterministic comparator output (OpenAI sections removed)
  - `ai-report.html`: only the AI-generated summaries for scenarios where OpenAI was actually triggered

Key files and components
------------------------
- `src/main/java/com/example/jsoncompare/JsonComparator.java` — core Java comparator, report generation, and OpenAI invocation orchestration.
- `src/main/java/com/example/jsoncompare/OpenAIHelper.java` — builds prompts and calls the OpenAI API.
- `src/main/java/com/example/jsoncompare/ScenarioResponseCollector.java` — helper to read JSON files and return filename->content maps.
- `src/test/resources/examples/compare.feature` — Karate feature that demonstrates loading the Java helpers and running scenario comparisons.
- Output (HTML reports): `src/test/resources/reports/overall-report.html` and `src/test/resources/reports/ai-report.html` (created after a run).

Prerequisites
-------------
- Java 11+ or a compatible JDK installed and on PATH.
- Maven 3.6+ (to build and run tests).
- (Optional) An OpenAI API key if you want the LLM summaries to be generated.

Configuration
-------------
1. OpenAI API key
   - The code checks for an API key in this order:
     1. `.env` file (key name `MY_KEY`),
     2. environment variable `MY_KEY`,
     3. environment variable `OPENAI_API_KEY`.
   - Example `.env` content (project root):
     MY_KEY=sk_XXXXXXXXXXXXXXXXXXXXXXXX

2. Threshold to trigger OpenAI
   - The threshold is defined in `JsonComparator.OPENAI_THRESHOLD`. If the number of structural differences for a scenario meets or exceeds this value, OpenAI is consulted.

3. Input folder
   - The Karate feature example uses `target/target/json-output/Feature1` as the folder with JSON responses grouped by scenario. Adjust as needed in `compare.feature`.

How to run
----------
From the project root (PowerShell examples):

- Run the test suite (Karate/JUnit tests):
  mvn test

- Run only the example comparison feature (if you want the same single-run behavior):
  mvn -Dtest=examples.SimpleTest test

After a successful run you will find the HTML reports here:
- `src/test/resources/reports/overall-report.html`
- `src/test/resources/reports/ai-report.html`

Open them in your browser (double-click in File Explorer or use `explorer <path>`).

What the reports contain
------------------------
- `overall-report.html`: All deterministic comparator output organized per scenario. "Comparison between ..." headers are emphasized and each diff is shown on its own line.
- `ai-report.html`: Only scenarios that actually triggered an OpenAI call are included. The AI summary sections are shown using the same font and styling as the overall report.

Why AI summaries might be missing
--------------------------------
- No API key found: make sure `MY_KEY` or `OPENAI_API_KEY` is set.
- The number of structural differences did not meet the configured threshold.
- Token limits / prompt length: for very large JSONs, the LLM may not have enough context. The code currently performs adjacent pairwise calls to mitigate this.

Troubleshooting
---------------
- "No files provided for scenario: <name>"
  - Verify the folder path and the presence of JSON files for that scenario. The file collector logs file reads when called in verbose mode.
- "No OpenAI API key found"
  - Place your key in `.env` (project root) or export environment variable `OPENAI_API_KEY`.
- If the HTML looks unstyled or fonts differ, ensure you open the generated files from the local filesystem — the CSS is embedded inline.

Extending the project
---------------------
- If you need guaranteed machine-readable AI output, change the prompts in `OpenAIHelper` to ask for structured JSON and parse the response.
- To make the reports prettier, add more CSS or export to other formats (PDF) in `JsonComparator.writeHtmlReports`.
- To decouple AI responses from the comparator output, store AI summaries in a separate map at call time (the code currently appends an AI section into the scenario output list and also generates separate HTML files).

Development notes
-----------------
- The comparator uses Jackson (`ObjectMapper`) to parse JSON files and a recursive diff algorithm to detect added/removed fields. It uses a small DTO `DiffResult` for per-diff info.
- The project contains a small helper `ScenarioResponseCollector` to read files and return a LinkedHashMap of filename → content to preserve order.

Contributing
------------
- Fork, create a feature branch, and open a pull request. Include tests for new behavior.

License & attribution
---------------------
- Add your preferred license here (e.g., MIT) before publishing to GitHub.

Contact / Help
--------------
- If you want, I can:
  - Add a MOCK mode for `OpenAIHelper` so you can run end-to-end tests without a real API key,
  - Convert AI output to structured JSON for deterministic parsing,
  - Improve report styling (collapsible sections, icons, download links).

Thank you — tell me if you want this saved into `README.md` in the project root and I will create the file for you.

