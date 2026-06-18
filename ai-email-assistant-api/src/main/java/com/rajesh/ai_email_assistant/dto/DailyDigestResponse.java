package com.rajesh.ai_email_assistant.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyDigestResponse {

    private int importantCount;
    private int workCount;
    private int personalCount;
    private int promotionCount;
    private List<EmailCategorySummaryDto> summary;
}
