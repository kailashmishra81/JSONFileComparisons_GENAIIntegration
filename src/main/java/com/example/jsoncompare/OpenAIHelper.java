package com.example.jsoncompare;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.*;
import java.util.stream.Collectors;

public class OpenAIHelper {

    /**
     * Accepts a Map of API call diffs and returns AI-analyzed summary.
     * @param summary Map of API call diffs
     * @return AI-generated explanation of changes
     */
    public static String getAiSummary(Map<String, String> summary,String apiKey) {
        if (summary == null || summary.isEmpty()) {
            return "No changes detected.";
        }

        // Flatten map into readable text for AI
        String prompt = summary.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));

        prompt = "You are an expert JSON analyzer.\n"
                + "Analyze the following JSON comparison results and explain the changes in simple terms:\n"
                + prompt
                + "Also mention the filenames involved."
                + "  \"Please explain what changed in simple terms and show the affected parts of the JSON. \" +\n" +
                "            \"Do not print unchanged sections.\""
                + "\nOnly explain what changed, no extra commentary.";

        // Read OpenAI API key from environment variable
//        String apiKey = System.getenv("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");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("OpenAI API key not set in environment variable OPENAI_API_KEY");
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
}
