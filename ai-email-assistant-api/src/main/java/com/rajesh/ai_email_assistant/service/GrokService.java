package com.rajesh.ai_email_assistant.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.rajesh.ai_email_assistant.config.GrokConfig;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
@Service
@RequiredArgsConstructor
public class GrokService {

    private final RestTemplate restTemplate;
    private final GrokConfig grokConfig;

    /**
     * Core method: sends a prompt to Grok API (OpenAI-compatible format)
     * and returns the response text.
     */
    public String generateContent(String prompt) {

        Map<String, Object> message = Map.of(
                "role", "user",
                "content", prompt
        );

        Map<String, Object> requestBody = Map.of(
                "model", grokConfig.getModel(),
                "messages", List.of(message)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(grokConfig.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                grokConfig.getApiUrl(), entity, Map.class);

        Map<String, Object> body = response.getBody();

        if (body == null) {
            return "";
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");

        if (choices == null || choices.isEmpty()) {
            return "";
        }

        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> messageObj = (Map<String, Object>) firstChoice.get("message");

        if (messageObj == null) {
            return "";
        }

        Object content = messageObj.get("content");
        return content != null ? content.toString().trim() : "";
    }

    public String summarizeEmail(String subject, String body) {

        String prompt = "Summarize the following email in 2-3 concise sentences. "
                + "Only return the summary text, nothing else.\n\n"
                + "Subject: " + subject + "\n\n"
                + "Body:\n" + body;

        return generateContent(prompt);
    }

    public String generateReply(String subject, String body) {

        String prompt = "Generate a professional email reply to the following email. "
                + "Only return the reply body text, no subject line, no extra commentary.\n\n"
                + "Subject: " + subject + "\n\n"
                + "Body:\n" + body;

        return generateContent(prompt);
    }

    public String classifyEmail(String subject, String body) {

        String prompt = "Classify the following email into exactly one of these categories: "
                + "IMPORTANT, WORK, PROMOTION, SPAM, PERSONAL. "
                + "Respond with only the single category word in uppercase, nothing else.\n\n"
                + "Subject: " + subject + "\n\n"
                + "Body:\n" + body;

        String result = generateContent(prompt);
        return sanitizeSingleWord(result);
    }

    public String detectPriority(String subject, String body) {

        String prompt = "Determine the priority level of the following email. "
                + "Respond with only one word: HIGH, MEDIUM, or LOW.\n\n"
                + "Subject: " + subject + "\n\n"
                + "Body:\n" + body;

        String result = generateContent(prompt);
        return sanitizeSingleWord(result);
    }

    private String sanitizeSingleWord(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().toUpperCase().replaceAll("[^A-Z]", "");
    }
}