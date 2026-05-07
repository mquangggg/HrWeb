package com.hrmanagement.hr_management.service;

import com.hrmanagement.hr_management.dto.request.NotificationRequest;
import com.hrmanagement.hr_management.dto.response.NotificationResponse;
import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getAllNotifications();
    NotificationResponse createNotification(NotificationRequest request, String email);
    void deleteNotification(Long id);
}
