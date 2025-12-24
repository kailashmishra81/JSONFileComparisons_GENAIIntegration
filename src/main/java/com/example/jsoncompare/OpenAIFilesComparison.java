//package com.example.jsoncompare;
//
//import io.github.cdimascio.dotenv.Dotenv;
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.io.IOException;
//
//public class OpenAIFilesComparison {
//
//    public static void main(String[] args) throws IOException {
//        // Minimal safe revert: load .env if present and print a message.
//        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
//        String apiKey = null;
//        try {
//            apiKey = dotenv.get("MY_KEY");
//        } catch (Exception ignored) {
//        }
//        if (apiKey == null || apiKey.isBlank()) {
//            apiKey = System.getenv("MY_KEY");
//        }
//
//        System.out.println("OpenAIFilesComparison - reverted safe stub");
//        if (apiKey == null || apiKey.isBlank()) {
//            System.out.println("No API key found (MY_KEY). OpenAI call disabled in stub.");
//        } else {
//            System.out.println("API key present (hidden). To enable network calls provide original file.");
//        }
//
//        // Optionally, read the report JSON files if you want to build a prompt later
//        try {
//            String json1 = Files.readString(Path.of("reports/File 1.json"));
//            // ... do nothing in stub
//        } catch (Exception ignored) {
//        }
//    }
//}
