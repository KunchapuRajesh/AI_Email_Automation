package com.rajesh.ai_email_assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDto {

    private String id;
    private String from;
    private String to;
    private String subject;
    private String body;
    private String snippet;
}
