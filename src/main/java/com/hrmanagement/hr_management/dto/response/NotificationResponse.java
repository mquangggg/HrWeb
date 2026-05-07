package com.hrmanagement.hr_management.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime publishedDate;
    private String publishedBy;
    private String type;
}
