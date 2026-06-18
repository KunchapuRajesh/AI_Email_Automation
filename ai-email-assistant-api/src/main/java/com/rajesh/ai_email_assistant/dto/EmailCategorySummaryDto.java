package com.rajesh.ai_email_assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailCategorySummaryDto {

    private String id;
    private String subject;
    private String category;
}
