package com.hrmanagement.hr_management.service.impl;

import com.hrmanagement.hr_management.dto.request.NotificationRequest;
import com.hrmanagement.hr_management.dto.response.NotificationResponse;
import com.hrmanagement.hr_management.entity.Employee;
import com.hrmanagement.hr_management.entity.Notification;
import com.hrmanagement.hr_management.repository.EmployeeRepository;
import com.hrmanagement.hr_management.repository.NotificationRepository;
import com.hrmanagement.hr_management.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAllByOrderByPublishedDateDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponse createNotification(NotificationRequest request, String email) {
        Employee publisher = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người đăng"));

        Notification notification = Notification.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .type(request.getType() != null ? request.getType() : "info")
                .publishedDate(LocalDateTime.now())
                .publishedBy(publisher.getFirstName() + " " + publisher.getLastName())
                .build();

        return mapToResponse(notificationRepository.save(notification));
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .publishedDate(notification.getPublishedDate())
                .publishedBy(notification.getPublishedBy())
                .type(notification.getType())
                .build();
    }
}
