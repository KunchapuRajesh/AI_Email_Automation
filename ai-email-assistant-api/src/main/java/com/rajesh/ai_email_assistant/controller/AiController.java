package com.rajesh.ai_email_assistant.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.rajesh.ai_email_assistant.dto.ClassifyResponse;
import com.rajesh.ai_email_assistant.dto.DailyDigestResponse;
import com.rajesh.ai_email_assistant.dto.EmailCategorySummaryDto;
import com.rajesh.ai_email_assistant.dto.EmailDto;
import com.rajesh.ai_email_assistant.dto.InboxSummaryItemDto;
import com.rajesh.ai_email_assistant.dto.MessageIdRequest;
import com.rajesh.ai_email_assistant.dto.PriorityResponse;
import com.rajesh.ai_email_assistant.dto.ReplyResponse;
import com.rajesh.ai_email_assistant.dto.SummaryResponse;
import com.rajesh.ai_email_assistant.service.GrokService;
import com.rajesh.ai_email_assistant.service.GmailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AiController {

    private static final int INBOX_FETCH_LIMIT = 20;

    private final GmailService gmailService;
    private final GrokService grokService;

    @PostMapping("/ai/summarize")
    public ResponseEntity<SummaryResponse> summarize(
            @RequestBody MessageIdRequest request,
            @RegisteredOAuth2AuthorizedClient("google")
            OAuth2AuthorizedClient client) {

        String token = client.getAccessToken().getTokenValue();

        EmailDto email = gmailService.getMessageDetails(token, request.getMessageId());

        String summary = grokService.summarizeEmail(email.getSubject(), email.getBody());

        return ResponseEntity.ok(SummaryResponse.builder().summary(summary).build());
    }

    @PostMapping("/ai/reply")
    public ResponseEntity<ReplyResponse> reply(
            @RequestBody MessageIdRequest request,
            @RegisteredOAuth2AuthorizedClient("google")
            OAuth2AuthorizedClient client) {

        String token = client.getAccessToken().getTokenValue();

        EmailDto email = gmailService.getMessageDetails(token, request.getMessageId());

        String reply = grokService.generateReply(email.getSubject(), email.getBody());

        return ResponseEntity.ok(ReplyResponse.builder().reply(reply).build());
    }

    @PostMapping("/ai/classify")
    public ResponseEntity<ClassifyResponse> classify(
            @RequestBody MessageIdRequest request,
            @RegisteredOAuth2AuthorizedClient("google")
            OAuth2AuthorizedClient client) {

        String token = client.getAccessToken().getTokenValue();

        EmailDto email = gmailService.getMessageDetails(token, request.getMessageId());

        String category = grokService.classifyEmail(email.getSubject(), email.getBody());

        return ResponseEntity.ok(ClassifyResponse.builder().category(category).build());
    }

    @PostMapping("/ai/priority")
    public ResponseEntity<PriorityResponse> priority(
            @RequestBody MessageIdRequest request,
            @RegisteredOAuth2AuthorizedClient("google")
            OAuth2AuthorizedClient client) {

        String token = client.getAccessToken().getTokenValue();

        EmailDto email = gmailService.getMessageDetails(token, request.getMessageId());

        String priority = grokService.detectPriority(email.getSubject(), email.getBody());

        return ResponseEntity.ok(PriorityResponse.builder().priority(priority).build());
    }

    @GetMapping("/ai/inbox-summary")
    public ResponseEntity<List<InboxSummaryItemDto>> inboxSummary(
            @RegisteredOAuth2AuthorizedClient("google")
            OAuth2AuthorizedClient client) {

        String token = client.getAccessToken().getTokenValue();

        List<EmailDto> emails = gmailService.getInboxMessages(token, INBOX_FETCH_LIMIT);

        List<InboxSummaryItemDto> result = new ArrayList<>();

        for (EmailDto email : emails) {

            String summary = grokService.summarizeEmail(email.getSubject(), email.getBody());

            result.add(InboxSummaryItemDto.builder()
                    .id(email.getId())
                    .subject(email.getSubject())
                    .summary(summary)
                    .build());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/ai/daily-digest")
    public ResponseEntity<DailyDigestResponse> dailyDigest(
            @RegisteredOAuth2AuthorizedClient("google")
            OAuth2AuthorizedClient client) {

        String token = client.getAccessToken().getTokenValue();

        List<EmailDto> emails = gmailService.getInboxMessages(token, INBOX_FETCH_LIMIT);

        int importantCount = 0;
        int workCount = 0;
        int personalCount = 0;
        int promotionCount = 0;

        List<EmailCategorySummaryDto> summaryList = new ArrayList<>();

        for (EmailDto email : emails) {

            String category = grokService.classifyEmail(email.getSubject(), email.getBody());

            switch (category) {
                case "IMPORTANT":
                    importantCount++;
                    break;
                case "WORK":
                    workCount++;
                    break;
                case "PERSONAL":
                    personalCount++;
                    break;
                case "PROMOTION":
                    promotionCount++;
                    break;
                default:
                    break;
            }

            summaryList.add(EmailCategorySummaryDto.builder()
                    .id(email.getId())
                    .subject(email.getSubject())
                    .category(category)
                    .build());
        }

        DailyDigestResponse response = DailyDigestResponse.builder()
                .importantCount(importantCount)
                .workCount(workCount)
                .personalCount(personalCount)
                .promotionCount(promotionCount)
                .summary(summaryList)
                .build();

        return ResponseEntity.ok(response);
    }
}

//package com.rajesh.ai_email_assistant.controller;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
//import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.rajesh.ai_email_assistant.dto.ClassifyResponse;
//import com.rajesh.ai_email_assistant.dto.DailyDigestResponse;
//import com.rajesh.ai_email_assistant.dto.EmailCategorySummaryDto;
//import com.rajesh.ai_email_assistant.dto.EmailDto;
//import com.rajesh.ai_email_assistant.dto.InboxSummaryItemDto;
//import com.rajesh.ai_email_assistant.dto.MessageIdRequest;
//import com.rajesh.ai_email_assistant.dto.PriorityResponse;
//import com.rajesh.ai_email_assistant.dto.ReplyResponse;
//import com.rajesh.ai_email_assistant.dto.SummaryResponse;
//import com.rajesh.ai_email_assistant.service.GeminiService;
//import com.rajesh.ai_email_assistant.service.GmailService;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequiredArgsConstructor
//public class AiController {
//
//    private static final int INBOX_FETCH_LIMIT = 20;
//
//    private final GmailService gmailService;
//    private final GeminiService geminiService;
//
//    @PostMapping("/ai/summarize")
//    public ResponseEntity<SummaryResponse> summarize(
//            @RequestBody MessageIdRequest request,
//            @RegisteredOAuth2AuthorizedClient("google")
//            OAuth2AuthorizedClient client) {
//
//        String token = client.getAccessToken().getTokenValue();
//
//        EmailDto email = gmailService.getMessageDetails(token, request.getMessageId());
//
//        String summary = geminiService.summarizeEmail(email.getSubject(), email.getBody());
//
//        return ResponseEntity.ok(SummaryResponse.builder().summary(summary).build());
//    }
//
//    @PostMapping("/ai/reply")
//    public ResponseEntity<ReplyResponse> reply(
//            @RequestBody MessageIdRequest request,
//            @RegisteredOAuth2AuthorizedClient("google")
//            OAuth2AuthorizedClient client) {
//
//        String token = client.getAccessToken().getTokenValue();
//
//        EmailDto email = gmailService.getMessageDetails(token, request.getMessageId());
//
//        String reply = geminiService.generateReply(email.getSubject(), email.getBody());
//
//        return ResponseEntity.ok(ReplyResponse.builder().reply(reply).build());
//    }
//
//    @PostMapping("/ai/classify")
//    public ResponseEntity<ClassifyResponse> classify(
//            @RequestBody MessageIdRequest request,
//            @RegisteredOAuth2AuthorizedClient("google")
//            OAuth2AuthorizedClient client) {
//
//        String token = client.getAccessToken().getTokenValue();
//
//        EmailDto email = gmailService.getMessageDetails(token, request.getMessageId());
//
//        String category = geminiService.classifyEmail(email.getSubject(), email.getBody());
//
//        return ResponseEntity.ok(ClassifyResponse.builder().category(category).build());
//    }
//
//    @PostMapping("/ai/priority")
//    public ResponseEntity<PriorityResponse> priority(
//            @RequestBody MessageIdRequest request,
//            @RegisteredOAuth2AuthorizedClient("google")
//            OAuth2AuthorizedClient client) {
//
//        String token = client.getAccessToken().getTokenValue();
//
//        EmailDto email = gmailService.getMessageDetails(token, request.getMessageId());
//
//        String priority = geminiService.detectPriority(email.getSubject(), email.getBody());
//
//        return ResponseEntity.ok(PriorityResponse.builder().priority(priority).build());
//    }
//
//    @GetMapping("/ai/inbox-summary")
//    public ResponseEntity<List<InboxSummaryItemDto>> inboxSummary(
//            @RegisteredOAuth2AuthorizedClient("google")
//            OAuth2AuthorizedClient client) {
//
//        String token = client.getAccessToken().getTokenValue();
//
//        List<EmailDto> emails = gmailService.getInboxMessages(token, INBOX_FETCH_LIMIT);
//
//        List<InboxSummaryItemDto> result = new ArrayList<>();
//
//        for (EmailDto email : emails) {
//
//            String summary = geminiService.summarizeEmail(email.getSubject(), email.getBody());
//
//            result.add(InboxSummaryItemDto.builder()
//                    .id(email.getId())
//                    .subject(email.getSubject())
//                    .summary(summary)
//                    .build());
//        }
//
//        return ResponseEntity.ok(result);
//    }
//
//    @GetMapping("/ai/daily-digest")
//    public ResponseEntity<DailyDigestResponse> dailyDigest(
//            @RegisteredOAuth2AuthorizedClient("google")
//            OAuth2AuthorizedClient client) {
//
//        String token = client.getAccessToken().getTokenValue();
//
//        List<EmailDto> emails = gmailService.getInboxMessages(token, INBOX_FETCH_LIMIT);
//
//        int importantCount = 0;
//        int workCount = 0;
//        int personalCount = 0;
//        int promotionCount = 0;
//
//        List<EmailCategorySummaryDto> summaryList = new ArrayList<>();
//
//        for (EmailDto email : emails) {
//
//            String category = geminiService.classifyEmail(email.getSubject(), email.getBody());
//
//            switch (category) {
//                case "IMPORTANT":
//                    importantCount++;
//                    break;
//                case "WORK":
//                    workCount++;
//                    break;
//                case "PERSONAL":
//                    personalCount++;
//                    break;
//                case "PROMOTION":
//                    promotionCount++;
//                    break;
//                default:
//                    break;
//            }
//
//            summaryList.add(EmailCategorySummaryDto.builder()
//                    .id(email.getId())
//                    .subject(email.getSubject())
//                    .category(category)
//                    .build());
//        }
//
//        DailyDigestResponse response = DailyDigestResponse.builder()
//                .importantCount(importantCount)
//                .workCount(workCount)
//                .personalCount(personalCount)
//                .promotionCount(promotionCount)
//                .summary(summaryList)
//                .build();
//
//        return ResponseEntity.ok(response);
//    }
//}
