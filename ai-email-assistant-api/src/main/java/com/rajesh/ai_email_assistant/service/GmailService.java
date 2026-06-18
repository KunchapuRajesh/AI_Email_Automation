package com.rajesh.ai_email_assistant.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.rajesh.ai_email_assistant.dto.EmailDto;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
@Service
@RequiredArgsConstructor
public class GmailService {

    private static final String GMAIL_BASE_URL = "https://gmail.googleapis.com/gmail/v1/users/me";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}");

    private final RestTemplate restTemplate;
    private final GmailParserService gmailParserService;

    public EmailDto getMessageDetails(String token, String messageId) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                GMAIL_BASE_URL + "/messages/" + messageId + "?format=full",
                HttpMethod.GET,
                entity,
                Map.class);

        Map<String, Object> gmailResponse = response.getBody();

        return gmailParserService.parse(gmailResponse);
    }

    public List<EmailDto> getInboxMessages(String token, int maxResults) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> listResponse = restTemplate.exchange(
                GMAIL_BASE_URL + "/messages?maxResults=" + maxResults,
                HttpMethod.GET,
                entity,
                Map.class);

        Map<String, Object> listBody = listResponse.getBody();

        List<EmailDto> emails = new ArrayList<>();

        if (listBody == null || listBody.get("messages") == null) {
            return emails;
        }

        List<Map<String, Object>> messages = (List<Map<String, Object>>) listBody.get("messages");

        for (Map<String, Object> message : messages) {
            String id = (String) message.get("id");
            emails.add(getMessageDetails(token, id));
        }

        return emails;
    }

    public String createDraft(String token, String to, String subject, String body, String threadId) {

        String rawMessage = buildRawMessage(to, subject, body);

        Map<String, Object> message = new HashMap<>();
        message.put("raw", rawMessage);

        if (threadId != null && !threadId.isEmpty()) {
            message.put("threadId", threadId);
        }

        Map<String, Object> draftRequest = new HashMap<>();
        draftRequest.put("message", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(draftRequest, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                GMAIL_BASE_URL + "/drafts",
                entity,
                Map.class);

        Map<String, Object> responseBody = response.getBody();

        return responseBody != null ? (String) responseBody.get("id") : null;
    }

    public void sendEmail(String token, String to, String subject, String body) {

        String rawMessage = buildRawMessage(to, subject, body);

        Map<String, Object> sendRequest = new HashMap<>();
        sendRequest.put("raw", rawMessage);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(sendRequest, headers);

        restTemplate.postForEntity(
                GMAIL_BASE_URL + "/messages/send",
                entity,
                Map.class);
    }

    public String extractEmailAddress(String fromHeader) {

        if (fromHeader == null) {
            return "";
        }

        Matcher matcher = EMAIL_PATTERN.matcher(fromHeader);

        if (matcher.find()) {
            return matcher.group();
        }

        return fromHeader;
    }

    public String buildReplySubject(String originalSubject) {

        if (originalSubject == null) {
            return "Re:";
        }

        String trimmed = originalSubject.trim();

        if (trimmed.toLowerCase().startsWith("re:")) {
            return trimmed;
        }

        return "Re: " + trimmed;
    }

    private String buildRawMessage(String to, String subject, String body) {

        String encodedSubject = "=?UTF-8?B?"
                + Base64.getEncoder().encodeToString(subject.getBytes(StandardCharsets.UTF_8))
                + "?=";

        StringBuilder message = new StringBuilder();
        message.append("To: ").append(to).append("\r\n");
        message.append("Subject: ").append(encodedSubject).append("\r\n");
        message.append("MIME-Version: 1.0\r\n");
        message.append("Content-Type: text/plain; charset=UTF-8\r\n");
        message.append("\r\n");
        message.append(body);

        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(message.toString().getBytes(StandardCharsets.UTF_8));
    }
}
