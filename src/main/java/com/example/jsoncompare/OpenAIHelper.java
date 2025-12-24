package com.example.jsoncompare;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class OpenAIHelper {

     // New preferred signature: accept a map of filename -> JSON content
     public static String getAiSummary(String scenario, Map<String, String> jsonContentsByFile, String apiKey) {
        if (jsonContentsByFile == null || jsonContentsByFile.isEmpty()) {
            return "No files provided for scenario: " + scenario;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Compare the following JSON files sequentially and explain the differences in simple terms.\n");
        sb.append("Instructions:\n");
        sb.append("1. Compare JSON1 vs JSON2 vs JSON3 and so on.\n");
        sb.append("2. For each comparison, identify added,removed, and changed fields, including nested changes.Mention which file the change belongs to.\n");
        sb.append("3. Additionally, track any fields that were removed in one file nd reappear in a later file.Highlight this pattern clrealy in the summary.\n");
      //  sb.append("4. Mention which file the change belongs to.\n\n");
        sb.append("5. Provide a short human resuable explaination for each change.\n\n");
        sb.append("6. Do not print unchanged parts of the JSON.\n\n");
        sb.append("Scenario: ").append(scenario).append("\n\n");

        // preserve the insertion order of files
        List<String> filenames = new ArrayList<>(jsonContentsByFile.keySet());
        for (int i = 0; i < filenames.size(); i++) {
            String fname = filenames.get(i);
            sb.append("--- BEGIN FILE: ").append(fname).append(" ---\n");
            sb.append(jsonContentsByFile.get(fname)).append("\n");
            sb.append("--- END FILE: ").append(fname).append(" ---\n\n");
        }

        sb.append("Please explain the summary of differences.");

        String prompt = sb.toString();

        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("OpenAI API key not set. Provide apiKey as argument or set OPENAI_API_KEY");
        }

        OpenAiService service = new OpenAiService(apiKey);
        ChatMessage message = new ChatMessage("user", prompt);
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(Collections.singletonList(message))
                .build();

        return service.createChatCompletion(request)
                .getChoices()
                .get(0)
                .getMessage()
                .getContent();
    }

//    // New helper: compare a single adjacent pair and return the formatted summary for that pair
//    public static String getAiSummaryForPair(String scenario, String fileName1, String content1, String fileName2, String content2, String apiKey) {
//        if ((content1 == null || content1.isBlank()) && (content2 == null || content2.isBlank())) {
//            return "No content provided for pair: " + fileName1 + " vs " + fileName2;
//        }
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("You are an expert JSON analyzer.\n");
//        sb.append("Compare these two JSON responses and produce a short human-readable section. IMPORTANT: use the actual filenames when labeling changed values, for example:\n");
//        sb.append("JSON File: ").append(fileName1).append(" vs ").append(fileName2).append("\n");
//        sb.append("Shipping Method\n");
//        sb.append(fileName1).append(": \"method\": \"Express\"\n");
//        sb.append(fileName2).append(": \"method\": \"FedEx\"\n");
//        sb.append("Explanation: The shipping method changed from \"Express\" to \"FedEx\" in ").append(fileName2).append(".\n\n");
//        sb.append("For each changed topic, follow the same pattern:\n");
//        sb.append("<Topic/Field Name>\n");
//        sb.append(fileName1).append(": <brief excerpt or 'Removed' or 'Added'>\n");
//        sb.append(fileName2).append(": <brief excerpt or 'Removed' or 'Added'>\n");
//        sb.append("Explanation: <one-sentence human-readable explanation>\n\n");
//        sb.append("Only include fields that changed. Prefer short excerpts, not full objects. Keep explanations concise.\n\n");
//
//        sb.append("--- BEGIN FILE: ").append(fileName1).append(" ---\n");
//        sb.append(content1 == null ? "" : content1).append("\n");
//        sb.append("--- END FILE: ").append(fileName1).append(" ---\n\n");
//
//        sb.append("--- BEGIN FILE: ").append(fileName2).append(" ---\n");
//        sb.append(content2 == null ? "" : content2).append("\n");
//        sb.append("--- END FILE: ").append(fileName2).append(" ---\n\n");
//
//        sb.append("Respond only with the requested sections, use the actual filenames as shown, and do NOT add anything outside the requested format.\n");
//
//        String prompt = sb.toString();
//
//        if (apiKey == null || apiKey.isEmpty()) {
//            throw new RuntimeException("OpenAI API key not set. Provide apiKey as argument or set OPENAI_API_KEY");
//        }
//
//        OpenAiService service = new OpenAiService(apiKey);
//        ChatMessage message = new ChatMessage("user", prompt);
//        ChatCompletionRequest request = ChatCompletionRequest.builder()
//                .model("gpt-3.5-turbo")
//                .messages(Collections.singletonList(message))
//                .build();
//
//        return service.createChatCompletion(request)
//                .getChoices()
//                .get(0)
//                .getMessage()
//                .getContent();
//    }

    // Backwards-compatible overload: accept file paths and read them then call the contents version
    public static String getAiSummaryFromFiles(String scenario, List<String> filePaths, String apiKey) {
        if (filePaths == null || filePaths.isEmpty()) {
            return "No files provided for scenario: " + scenario;
        }
        LinkedHashMap<String, String> contents = new LinkedHashMap<>();
        for (String p : filePaths) {
            try {
                contents.put(Path.of(p).getFileName().toString(), Files.readString(Path.of(p)));
            } catch (IOException e) {
                contents.put(Path.of(p).getFileName().toString(), "\n[Failed to read file: " + p + " -> " + e.getMessage() + "]\n");
            }
        }
        return getAiSummary(scenario, contents, apiKey);
    }
}
