package com.rajesh.ai_email_assistant.controller;

import java.util.List;

import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.rajesh.ai_email_assistant.dto.DraftResponse;
import com.rajesh.ai_email_assistant.dto.EmailDto;
import com.rajesh.ai_email_assistant.dto.MessageIdRequest;
import com.rajesh.ai_email_assistant.dto.SendEmailRequest;
import com.rajesh.ai_email_assistant.dto.SendEmailResponse;
import com.rajesh.ai_email_assistant.service.GrokService;
import com.rajesh.ai_email_assistant.service.GmailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GmailController {

    private final OAuth2AuthorizedClientService clientService;
    private final RestTemplate restTemplate;
    private final GmailService gmailService;
    private final GrokService grokService;

    @GetMapping("/token")
    public String token(
            @RegisteredOAuth2AuthorizedClient("google")
            OAuth2AuthorizedClient client,
            OAuth2AuthenticationToken authentication) {

        return client.getAccessToken().getTokenValue();
    }

    @GetMapping("/gmail/profile")
    public String profile(
            @RegisteredOAuth2AuthorizedClient("google")
            OAuth2AuthorizedClient client) {

        String token = client.getAccessToken().getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "https://gmail.googleapis.com/gmail/v1/users/me/profile",
                        HttpMethod.GET,
                        entity,
                        String.class);

        return response.getBody();
    }

    @GetMapping("/gmail/messages")
    public ResponseEntity<?> messages(
            @RegisteredOAuth2AuthorizedClient("google")
            OAuth2AuthorizedClient client) {

        try {

            String token = client.getAccessToken().getTokenValue();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            "https://gmail.googleapis.com/gmail/v1/users/me/messages?maxResults=10",
                            HttpMethod.GET,
                            entity,
                            String.class);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/gmail/inbox")
    public ResponseEntity<List<EmailDto>> inbox(
            @RequestParam(defaultValue = "10") int maxResults,
            @RegisteredOAuth2AuthorizedClient("google")
            OAuth2AuthorizedClient client) {

        String token = client.getAccessToken().getTokenValue();

        List<EmailDto> emails = gmailService.getInboxMessages(token, maxResults);

        return ResponseEntity.ok(emails);
    }

    @GetMapping("/gmail/message/{messageId}")
    public ResponseEntity<EmailDto> getMessage(
            @PathVariable String messageId,
            @RegisteredOAuth2AuthorizedClient("google")
            OAuth2AuthorizedClient client) {

        String token = client.getAccessToken().getTokenValue();

        EmailDto emailDto = gmailService.getMessageDetails(token, messageId);

        return ResponseEntity.ok(emailDto);
    }

    @PostMapping("/gmail/draft")
    public ResponseEntity<DraftResponse> createDraft(
            @RequestBody MessageIdRequest request,
            @RegisteredOAuth2AuthorizedClient("google")
            OAuth2AuthorizedClient client) {

        String token = client.getAccessToken().getTokenValue();

        EmailDto original = gmailService.getMessageDetails(token, request.getMessageId());

        String replyBody = grokService.generateReply(original.getSubject(), original.getBody());

        String to = gmailService.extractEmailAddress(original.getFrom());
        String subject = gmailService.buildReplySubject(original.getSubject());

        String draftId = gmailService.createDraft(token, to, subject, replyBody, null);

        return ResponseEntity.ok(DraftResponse.builder().draftId(draftId).build());
    }

    @PostMapping("/gmail/send")
    public ResponseEntity<SendEmailResponse> sendEmail(
            @RequestBody SendEmailRequest request,
            @RegisteredOAuth2AuthorizedClient("google")
            OAuth2AuthorizedClient client) {

        String token = client.getAccessToken().getTokenValue();

        gmailService.sendEmail(token, request.getTo(), request.getSubject(), request.getBody());

        return ResponseEntity.ok(SendEmailResponse.builder().status("SENT").build());
    }

    @PostMapping("/gmail/auto-reply")
    public ResponseEntity<SendEmailResponse> autoReply(
            @RequestBody MessageIdRequest request,
            @RegisteredOAuth2AuthorizedClient("google")
            OAuth2AuthorizedClient client) {

        String token = client.getAccessToken().getTokenValue();

        EmailDto original = gmailService.getMessageDetails(token, request.getMessageId());

        String replyBody = grokService.generateReply(original.getSubject(), original.getBody());

        String to = gmailService.extractEmailAddress(original.getFrom());
        String subject = gmailService.buildReplySubject(original.getSubject());

        gmailService.sendEmail(token, to, subject, replyBody);

        return ResponseEntity.ok(SendEmailResponse.builder().status("SENT").build());
    }
}