package com.example.jsoncompare;

import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class OpenAIFilesComparison {

    public static void main(String[] args) throws IOException {
        Dotenv dotenv = Dotenv.load();

        // Get the OpenAI API key
        String apiKey = dotenv.get("MY_KEY");
   //     OpenAiService service = new OpenAiService(apiKey);
        OpenAiService service = new OpenAiService(apiKey, Duration.ofMinutes(3));


        // 2️⃣ Read two JSON files
        String json1 = Files.readString(Path.of("reports/File 1.json"));
        String json2 = Files.readString(Path.of("reports/File 2.json"));
        String json3 = Files.readString(Path.of("reports/File 3.json"));
        String json4 = Files.readString(Path.of("reports/File 4.json"));

       String prompt =
                "Compare the following JSON files sequentially and explain the differences in simple terms.\n" +
                        "Instructions:\n" +
                        "1. Show only the fields that changed, were added, or removed.\n\n" +
   //                     "2. Include the JSON snippet for each change.\n\n" +
                        "3. Mention which file the change belongs to.\n\n" +
                        "4. Provide a short human-readable explanation for each change.\n\n" +
                        "5. Do not print unchanged parts of the JSON.\n\n" +
        "Show only changed parts and mention which file they belong to.\n\n" +
                "JSON File 1:\n" + json1 + "\n\n" +
                "JSON File 2:\n" + json2 + "\n\n" +
                "JSON File 3:\n" + json3 + "\n\n" +
                "JSON File 4:\n" + json4 + "\n\n" +
                "Explain the summary of differences:";



        ChatMessage userMessage = new ChatMessage("user", prompt);
        ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                .model("gpt-4o")  // or "gpt-3.5-turbo"
                .messages(List.of(userMessage))
                .temperature(0.2)
                .build();


        ChatCompletionResult result = service.createChatCompletion(chatRequest);


        System.out.println("🧾 JSON Comparison Summary:\n");
        System.out.println(result.getChoices().get(0).getMessage().getContent());
    }
}
