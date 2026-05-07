package com.hrmanagement.hr_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @Column(name = "published_by")
    private String publishedBy;

    // Type: info, warning, success, danger
    @Column(nullable = false)
    private String type;
}
