package com.hrmanagement.hr_management.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String title;
    private String content;
    private String type; // info, warning, success, danger
}
