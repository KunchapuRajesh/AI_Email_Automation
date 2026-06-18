//package com.rajesh.ai_email_assistant.service;
//
//import java.util.List;
//import java.util.Map;
//
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import com.rajesh.ai_email_assistant.config.GeminiConfig;
//
//import lombok.RequiredArgsConstructor;
//
//@SuppressWarnings("unchecked")
//@Service
//@RequiredArgsConstructor
//public class GeminiService {
//
//    private final RestTemplate restTemplate;
//    private final GeminiConfig geminiConfig;
//
//    public String generateContent(String prompt) {
//
//        String url = geminiConfig.getApiBaseUrl()
//                + "/" + geminiConfig.getModel()
//                + ":generateContent?key=" + geminiConfig.getApiKey();
//
//        Map<String, Object> part = Map.of("text", prompt);
//        Map<String, Object> content = Map.of("parts", List.of(part));
//        Map<String, Object> requestBody = Map.of("contents", List.of(content));
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
//
//        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
//
//        Map<String, Object> body = response.getBody();
//
//        if (body == null) {
//            return "";
//        }
//
//        List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
//
//        if (candidates == null || candidates.isEmpty()) {
//            return "";
//        }
//
//        Map<String, Object> firstCandidate = candidates.get(0);
//        Map<String, Object> candidateContent = (Map<String, Object>) firstCandidate.get("content");
//
//        if (candidateContent == null) {
//            return "";
//        }
//
//        List<Map<String, Object>> parts = (List<Map<String, Object>>) candidateContent.get("parts");
//
//        if (parts == null || parts.isEmpty()) {
//            return "";
//        }
//
//        Object text = parts.get(0).get("text");
//
//        return text != null ? text.toString().trim() : "";
//    }
//
//    public String summarizeEmail(String subject, String body) {
//
//        String prompt = "Summarize the following email in 2-3 concise sentences. "
//                + "Only return the summary text, nothing else.\n\n"
//                + "Subject: " + subject + "\n\n"
//                + "Body:\n" + body;
//
//        return generateContent(prompt);
//    }
//
//    public String generateReply(String subject, String body) {
//
//        String prompt = "Generate a professional email reply to the following email. "
//                + "Only return the reply body text, no subject line, no extra commentary.\n\n"
//                + "Subject: " + subject + "\n\n"
//                + "Body:\n" + body;
//
//        return generateContent(prompt);
//    }
//
//    public String classifyEmail(String subject, String body) {
//
//        String prompt = "Classify the following email into exactly one of these categories: "
//                + "IMPORTANT, WORK, PROMOTION, SPAM, PERSONAL. "
//                + "Respond with only the single category word in uppercase, nothing else.\n\n"
//                + "Subject: " + subject + "\n\n"
//                + "Body:\n" + body;
//
//        String result = generateContent(prompt);
//
//        return sanitizeSingleWord(result);
//    }
//
//    public String detectPriority(String subject, String body) {
//
//        String prompt = "Determine the priority level of the following email. "
//                + "Respond with only one word: HIGH, MEDIUM, or LOW.\n\n"
//                + "Subject: " + subject + "\n\n"
//                + "Body:\n" + body;
//
//        String result = generateContent(prompt);
//
//        return sanitizeSingleWord(result);
//    }
//
//    private String sanitizeSingleWord(String text) {
//
//        if (text == null) {
//            return "";
//        }
//
//        return text.trim().toUpperCase().replaceAll("[^A-Z]", "");
//    }
//}
