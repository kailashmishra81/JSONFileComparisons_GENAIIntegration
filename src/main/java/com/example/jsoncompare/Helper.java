package com.example.jsoncompare;

import java.util.List;

public static class DiffResult {
    public int count;
    public List<String> messages;

    public DiffResult(int count, List<String> messages) {
        this.count = count;
        this.messages = messages;
    }
}