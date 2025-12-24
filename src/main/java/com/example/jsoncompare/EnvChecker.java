package com.example.jsoncompare;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvChecker {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String apiKey = null;
        try {
            apiKey = dotenv.get("MY_KEY");
        } catch (Exception ignored) {}
        if (apiKey == null || apiKey.isBlank()) apiKey = System.getenv("MY_KEY");
        if (apiKey == null || apiKey.isBlank()) apiKey = System.getenv("OPENAI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("ENV CHECK: No API key found (MY_KEY or OPENAI_API_KEY)");
            System.out.println(".env present? " + (EnvChecker.class.getResource("/../../../../.env") != null));
            return;
        }

        String masked;
        if (apiKey.length() > 12) {
            masked = apiKey.substring(0, 6) + "..." + apiKey.substring(apiKey.length() - 4);
        } else {
            masked = "(loaded, masked)";
        }
        System.out.println("ENV CHECK: API key loaded and masked: " + masked);
        System.out.println("Note: This does NOT reveal your full key.");
    }
}

