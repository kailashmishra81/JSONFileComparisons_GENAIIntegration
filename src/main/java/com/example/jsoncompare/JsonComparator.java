package com.example.jsoncompare;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class JsonComparator {

    // ✅ Keep mapper private – no access issues now
    private static final ObjectMapper mapper = new ObjectMapper();

    // Threshold for triggering OpenAI and for selecting per-pair diffs
    private static final int OPENAI_THRESHOLD = 3;


    private static boolean askUserToTriggerOpenAI(String scenario, int diffCount) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n⚠ Scenario '" + scenario + "' has "
                + diffCount + " differences.");
        System.out.print("Do you want to trigger OpenAI for this scenario? (y/n): ");

        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("y") || input.equals("yes");
    }


    // ✅ DTO for a single difference
    public static class DiffResult {
        String path;
        String change;
        String fileName;

        public DiffResult(String path, String change, String fileName) {
            this.path = path;
            this.change = change;
            this.fileName = fileName;
        }

        @Override
        public String toString() {
            return path + " → " + change + " in " + fileName;
        }
    }

    // ✅ Compare two JSON files (detailed)
    private static List<DiffResult> compareTwoJsonFilesDetailed(
            String file1Path,
            String file2Path) throws Exception {

        JsonNode json1 = mapper.readTree(new File(file1Path));
        JsonNode json2 = mapper.readTree(new File(file2Path));

        List<DiffResult> diffs = new ArrayList<>();
        findDifferences(json1, json2, "", diffs, new File(file2Path).getName());
        return diffs;
    }

    // ✅ Recursive diff finder
    private static void findDifferences(
            JsonNode json1,
            JsonNode json2,
            String path,
            List<DiffResult> diffs,
            String targetFile) {

        Iterator<String> fields = json1.fieldNames();

        // Removed fields
        while (fields.hasNext()) {
            String field = fields.next();
            String currentPath = path.isEmpty() ? field : path + "." + field;

            if (!json2.has(field)) {
                diffs.add(new DiffResult(currentPath, "Removed", targetFile));
            } else if (json1.get(field).isObject() && json2.get(field).isObject()) {
                findDifferences(json1.get(field), json2.get(field),
                        currentPath, diffs, targetFile);
            }
        }

        // Added fields
        Iterator<String> newFields = json2.fieldNames();
        while (newFields.hasNext()) {
            String field = newFields.next();
            String currentPath = path.isEmpty() ? field : path + "." + field;

            if (!json1.has(field)) {
                diffs.add(new DiffResult(currentPath, "Added", targetFile));
            }
        }
    }

    // ✅ Compare all scenarios
    public static Map<String, List<String>> compareAllScenarios(
            Map<String, List<String>> groupedFiles) throws Exception {

        Map<String, List<String>> finalResult = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : groupedFiles.entrySet()) {

            String scenario = entry.getKey();
            List<String> files = entry.getValue();
            // Ensure files are in deterministic order (e.g., chronological by filename) so adjacent comparisons are meaningful
            files.sort(Comparator.comparing(f -> new File(f).getName()));

            List<String> output = new ArrayList<>();

            if (files.size() < 2) {
                output.add("Only one file, no comparison");
                finalResult.put(scenario, output);
                continue;
            }

            int diffCount = 0;

            for (int i = 0; i < files.size() - 1; i++) {
                String f1 = files.get(i);
                String f2 = files.get(i + 1);

                output.add("Comparison between "
                        + new File(f1).getName()
                        + " and "
                        + new File(f2).getName()
                        + ":");

                List<DiffResult> diffs = compareTwoJsonFilesDetailed(f1, f2);

                if (diffs.isEmpty()) {
                    output.add("No differences");
                } else {
                    for (DiffResult d : diffs) {
                        output.add(d.toString());
                    }
                }
                diffCount += diffs.size();
            }

            // ✅ Agentic decision point
            if (diffCount >= OPENAI_THRESHOLD) {
                // Load API key from .env or environment
                io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure().ignoreIfMissing().load();
                String apiKey = null;
                try { apiKey = dotenv.get("MY_KEY"); } catch (Exception ignored) {}
                if (apiKey != null) {
                    apiKey = apiKey.trim();
                    if ((apiKey.startsWith("\"") && apiKey.endsWith("\"")) || (apiKey.startsWith("'") && apiKey.endsWith("'"))) {
                        apiKey = apiKey.substring(1, apiKey.length() - 1);
                    }
                }
                if (apiKey == null || apiKey.isBlank()) apiKey = System.getenv("MY_KEY");
                if (apiKey == null || apiKey.isBlank()) apiKey = System.getenv("OPENAI_API_KEY");

                if (apiKey == null || apiKey.isBlank()) {
                    System.out.println("No OpenAI API key found in environment (MY_KEY or OPENAI_API_KEY). Skipping OpenAI call.");
                } else {
                    // Collect JSON contents for all files in this scenario and call the OpenAI helper
                    LinkedHashMap<String, String> jsonMap = ScenarioResponseCollector.collectResponsesFromFiles(files, scenario);
                    if (jsonMap.isEmpty()) {
                        System.out.println("No files provided for scenario: " + scenario);
                    } else {
                        String aiSummary = OpenAIHelper.getAiSummary(scenario, jsonMap, apiKey);
                        output.add("OpenAI triggered (diff count = " + diffCount + ")");
                        if (aiSummary != null && !aiSummary.isBlank()) {
                            output.add("--- OpenAI Summary for scenario: " + scenario + " ---");
                            String[] aiLines = aiSummary.split("\\r?\\n");
                            for (String l : aiLines) {
                                if (l != null && !l.isBlank()) output.add(l);
                            }
                            output.add("--- End of OpenAI Summary ---");
                        }
                    }
                }
            }

            finalResult.put(scenario, output);
        }

        // After processing all scenarios, write two HTML reports: overall (excluding OpenAI sections) and AI-only
        try {
            writeHtmlReports(finalResult);
        } catch (Exception e) {
            System.err.println("Failed to write HTML reports: " + e.getMessage());
            e.printStackTrace(System.err);
        }

        return finalResult;
    }

    // Write two HTML files under src/test/resources/reports:
    // - overall-report.html (all comparator output with OpenAI sections removed)
    // - ai-report.html (only the OpenAI summary sections)
    private static void writeHtmlReports(Map<String, List<String>> finalResult) throws Exception {
        Path outDir = Paths.get("src", "test", "resources", "reports");
        Files.createDirectories(outDir);

        StringBuilder overall = new StringBuilder();
        StringBuilder aiOnly = new StringBuilder();

        overall.append("<html><head><meta charset=\"utf-8\"><title>Overall Comparison Report</title>");
        // Add simple CSS to style comparison headers and AI blocks
        overall.append("<style>");
        overall.append("body{font-family: Arial, Helvetica, sans-serif; margin:20px; font-size:18px;}\n");
        overall.append(".comparison{font-weight:700; font-size:1.45em; margin:12px 0; display:block;color:#111;}\n");
        overall.append(".scenario{margin-bottom:16px;}\n");
        overall.append(".diff{margin-left:12px; color:#333; line-height:1.6; font-size:1.05em;}\n");
        overall.append(".line{margin-left:6px; color:#444; font-size:1.02em;}\n");
        overall.append("pre{background:#f8f8f8; padding:12px; border-radius:6px; overflow:auto;}\n");
        overall.append(".ai-line{color:#2a6f97; font-size:1.05em; line-height:1.6;}\n");
        overall.append("</style>");
        overall.append("</head><body>");
        overall.append("<h1>Overall Comparison Report (excluding OpenAI)</h1>");

        aiOnly.append("<html><head><meta charset=\"utf-8\"><title>AI Summary Report</title>");
        // use the same CSS as the overall report so font-type and sizes match
        aiOnly.append("<style>");
        aiOnly.append("body{font-family: Arial, Helvetica, sans-serif; margin:20px; font-size:18px;}\n");
        aiOnly.append(".comparison{font-weight:700; font-size:1.45em; margin:12px 0; display:block;color:#111;}\n");
        aiOnly.append(".scenario{margin-bottom:16px;}\n");
        aiOnly.append(".diff{margin-left:12px; color:#333; line-height:1.6; font-size:1.05em;}\n");
        aiOnly.append(".line{margin-left:6px; color:#444; font-size:1.02em;}\n");
        aiOnly.append("pre{background:#f4fbff; padding:12px; border-radius:6px; overflow:auto;}\n");
        aiOnly.append(".ai-line{color:#2a6f97; font-size:1.05em; line-height:1.6;}\n");
        aiOnly.append("</style>");
        aiOnly.append("</head><body>");
        aiOnly.append("<h1>AI Summary Report</h1>");

        for (Map.Entry<String, List<String>> e : finalResult.entrySet()) {
            String scenario = e.getKey();
            List<String> lines = e.getValue();

            overall.append("<h2>").append(escapeHtml(scenario)).append("</h2>");
            overall.append("<div class='scenario'>");

            // Collect AI lines for this scenario separately and only append to aiOnly if the AI marker was present and AI was triggered
            StringBuilder perScenarioAi = new StringBuilder();
            boolean inAi = false;
            boolean hasAiMarker = false;
            boolean triggeredFound = false;
            for (String line : lines) {
                if (line == null) continue;
                if (line.startsWith("--- OpenAI Summary")) { inAi = true; hasAiMarker = true; continue; }
                if (line.startsWith("--- End of OpenAI Summary")) { inAi = false; continue; }
                if (line.startsWith("OpenAI triggered")) { triggeredFound = true; /* skip in overall; could log in ii */ continue; }

                if (inAi) {
                    // AI lines styled in ai-report (class available there)
                    perScenarioAi.append("<div class='ai-line'>").append(escapeHtml(line)).append("</div>");
                } else {
                    // If this is a comparison header, wrap it in a styled div so it's bold and larger
                    if (line.startsWith("Comparison between ")) {
                        overall.append("<div class='comparison'>").append(escapeHtml(line)).append("</div>");
                    } else if (line.contains(" → ")) {
                        // Diff produced by the comparator
                        overall.append("<div class='diff'>").append(escapeHtml(line)).append("</div>");
                    } else {
                        overall.append("<div class='line'>").append(escapeHtml(line)).append("</div>");
                    }
                }
            }

            overall.append("</div>");

            // Only append scenario header + content to the AI-only report when AI marker was present
            // and the 'OpenAI triggered' line was present (ensures OpenAI was actually invoked for this scenario)
            if (hasAiMarker && triggeredFound && perScenarioAi.length() > 0) {
                 aiOnly.append("<div class='scenario'>");
                 aiOnly.append("<h2>").append(escapeHtml(scenario)).append("</h2>");
                 aiOnly.append(perScenarioAi);
                 aiOnly.append("</div>");
             }
        }

        overall.append("</body></html>");
        aiOnly.append("</body></html>");

        Files.writeString(outDir.resolve("overall-report.html"), overall.toString(), StandardCharsets.UTF_8);
        Files.writeString(outDir.resolve("ai-report.html"), aiOnly.toString(), StandardCharsets.UTF_8);

        System.out.println("Wrote reports to: " + outDir.toAbsolutePath());
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

}
