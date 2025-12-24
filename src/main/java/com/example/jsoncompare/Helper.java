//package com.example.jsoncompare;
//
//import com.fasterxml.jackson.databind.JsonNode;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//import static com.example.jsoncompare.JsonComparator.findDifferences;
//import static com.example.jsoncompare.JsonComparator.mapper;
//
//public class Helper {
//    public static class DiffResult {
//        public int count;
//        public List<String> messages;
//
//        public DiffResult(int count, List<String> messages) {
//            this.count = count;
//            this.messages = messages;
//        }
//    }
//
//    public static DiffResult compareTwoJsonFilesWithCount(
//            String filePath1, String filePath2) throws Exception {
//
//        JsonNode json1 = mapper.readTree(new File(filePath1));
//        JsonNode json2 = mapper.readTree(new File(filePath2));
//
//        Map<String, Object> differences = new LinkedHashMap<>();
//        findDifferences(json1, json2, differences, "");
//
//        List<String> messages = new ArrayList<>();
//
//        for (Map.Entry<String, Object> entry : differences.entrySet()) {
//            messages.add(entry.getKey() + " → " + entry.getValue()
//                    + " in " + new File(filePath2).getName());
//        }
//
//        return new DiffResult(differences.size(), messages);
//    }
//
//}