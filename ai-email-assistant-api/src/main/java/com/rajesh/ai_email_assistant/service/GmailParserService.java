package com.rajesh.ai_email_assistant.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.rajesh.ai_email_assistant.dto.EmailDto;

@SuppressWarnings("unchecked")
@Service
public class GmailParserService {

    public EmailDto parse(Map<String, Object> gmailResponse) {

        Map<String, Object> payload = (Map<String, Object>) gmailResponse.get("payload");

        List<Map<String, String>> headers =
                (List<Map<String, String>>) payload.get("headers");

        String from = "";
        String to = "";
        String subject = "";

        if (headers != null) {
            for (Map<String, String> header : headers) {

                switch (header.get("name")) {

                    case "From":
                        from = header.get("value");
                        break;

                    case "To":
                        to = header.get("value");
                        break;

                    case "Subject":
                        subject = header.get("value");
                        break;
                }
            }
        }

        String body = extractBody(payload);

        return EmailDto.builder()
                .id((String) gmailResponse.get("id"))
                .from(from)
                .to(to)
                .subject(subject)
                .body(body)
                .snippet((String) gmailResponse.get("snippet"))
                .build();
    }

    private String extractBody(Map<String, Object> payload) {

        if (payload == null) {
            return "";
        }

        List<Map<String, Object>> parts = (List<Map<String, Object>>) payload.get("parts");

        if (parts != null && !parts.isEmpty()) {

            String plainText = null;
            String htmlText = null;

            for (Map<String, Object> part : parts) {

                String partMimeType = (String) part.get("mimeType");
                String extracted = extractBody(part);

                if (extracted == null || extracted.isEmpty()) {
                    continue;
                }

                if ("text/plain".equals(partMimeType)) {
                    plainText = extracted;
                } else if ("text/html".equals(partMimeType)) {
                    htmlText = extracted;
                } else if (plainText == null) {
                    plainText = extracted;
                }
            }

            if (plainText != null) {
                return plainText;
            }

            return htmlText != null ? htmlText : "";
        }

        Map<String, Object> body = (Map<String, Object>) payload.get("body");

        if (body != null && body.get("data") != null) {
            return decodeBase64Url((String) body.get("data"));
        }

        return "";
    }

    private String decodeBase64Url(String data) {

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(data);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return "";
        }
    }
}
