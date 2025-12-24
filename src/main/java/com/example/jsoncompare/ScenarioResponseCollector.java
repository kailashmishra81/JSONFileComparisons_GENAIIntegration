package com.example.jsoncompare;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.IOException;

/**
 * Helper to collect JSON response contents from a list of file paths for a scenario.
 * Keeps I/O separate from AI logic and adds diagnostic logging.
 */
public class ScenarioResponseCollector {

    // Public no-arg logging version: silent by default
    public static LinkedHashMap<String, String> collectResponsesFromFiles(List<String> filePaths, String scenario) {
        return collectResponsesFromFiles(filePaths, scenario, false);
    }

    // Main implementation with optional verbose logging
    public static LinkedHashMap<String, String> collectResponsesFromFiles(List<String> filePaths, String scenario, boolean verbose) {
        LinkedHashMap<String, String> results = new LinkedHashMap<>();

        if (verbose) {
            System.out.println("[ScenarioResponseCollector] Working dir: " + System.getProperty("user.dir"));
            System.out.println("[ScenarioResponseCollector] Scenario: '" + scenario + "'");
        }

        if (filePaths == null || filePaths.isEmpty()) {
            if (verbose) System.out.println("[ScenarioResponseCollector] No file paths provided.");
            return results;
        }

        for (String p : filePaths) {
            if (p == null) continue;
            Path path = Path.of(p);
            try {
                if (!Files.exists(path)) {
                    if (verbose) System.out.println("[ScenarioResponseCollector] Missing file: " + path.toAbsolutePath());
                    continue;
                }
                String content = Files.readString(path, StandardCharsets.UTF_8);
                if (content.isBlank()) {
                    if (verbose) System.out.println("[ScenarioResponseCollector] File is empty: " + path.getFileName());
                    continue;
                }
                results.put(path.getFileName().toString(), content);
                if (verbose) System.out.println("[ScenarioResponseCollector] Read file: " + path.getFileName());
            } catch (IOException e) {
                if (verbose) System.out.println("[ScenarioResponseCollector] Failed to read file: " + p + " -> " + e.getMessage());
            }
        }

        if (verbose) {
            if (results.isEmpty()) {
                System.out.println("[ScenarioResponseCollector] No readable JSON contents were collected for scenario: " + scenario);
            } else {
                System.out.println("[ScenarioResponseCollector] Collected " + results.size() + " JSON contents for scenario: " + scenario);
            }
        }

        return results;
    }
}
