package com.hrmanagement.hr_management.service.impl;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hrmanagement.hr_management.dto.response.LeaveResponse;
import com.hrmanagement.hr_management.dto.response.PageResponse;
import com.hrmanagement.hr_management.entity.Employee;
import com.hrmanagement.hr_management.enums.LeaveStatus;
import com.hrmanagement.hr_management.repository.EmployeeRepository;
import com.hrmanagement.hr_management.repository.LeaveRequestRepository;
import com.hrmanagement.hr_management.service.LeaveRequestService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public LeaveResponse createLeaveRequest(String email, com.hrmanagement.hr_management.dto.request.LeaveRequest request) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        if (request.getStartDate().isBefore(java.time.LocalDate.now())) {
            throw new RuntimeException("Không thể xin nghỉ phép cho những ngày trong quá khứ!");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu!");
        }

        com.hrmanagement.hr_management.entity.LeaveRequest leaveRequest = new com.hrmanagement.hr_management.entity.LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setReason(request.getReason());
        leaveRequest.setStatus(LeaveStatus.pending);

        com.hrmanagement.hr_management.entity.LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(saved);
    }

    @Override
    public PageResponse<LeaveResponse> getMyLeaveRequests(String email, int page, int size) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<com.hrmanagement.hr_management.entity.LeaveRequest> pageData = leaveRequestRepository.findByEmployeeId(employee.getId(), pageable);

        return mapToPageResponse(pageData);
    }

    @Override
    public PageResponse<LeaveResponse> getAllLeaveRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<com.hrmanagement.hr_management.entity.LeaveRequest> pageData = leaveRequestRepository.findAll(pageable);

        return mapToPageResponse(pageData);
    }

    @Override
    public LeaveResponse updateStatus(Long id, String email, LeaveStatus newStatus) {
        Employee approver = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người duyệt"));

        com.hrmanagement.hr_management.entity.LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn xin nghỉ phép"));

        if (leaveRequest.getStatus() != LeaveStatus.pending) {
            throw new RuntimeException("Đơn này đã được xử lý (không còn ở trạng thái pending)!");
        }

        leaveRequest.setStatus(newStatus);
        leaveRequest.setApprovedBy(approver);

        com.hrmanagement.hr_management.entity.LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(updated);
    }

    private LeaveResponse mapToResponse(com.hrmanagement.hr_management.entity.LeaveRequest entity) {
        long daysBetween = ChronoUnit.DAYS.between(entity.getStartDate(), entity.getEndDate()) + 1;
        
        return LeaveResponse.builder()
                .id(entity.getId())
                .fullName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName())
                .status(entity.getStatus().name())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .reason(entity.getReason())
                .days((int) daysBetween)
                .approverByName(entity.getApprovedBy() != null ? 
                        entity.getApprovedBy().getFirstName() + " " + entity.getApprovedBy().getLastName() : null)
                .note("") // Có thể thêm logic lưu note khi từ chối nếu có
                .build();
    }

    private PageResponse<LeaveResponse> mapToPageResponse(Page<com.hrmanagement.hr_management.entity.LeaveRequest> pageData) {
        List<LeaveResponse> dtoList = pageData.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<LeaveResponse>builder()
                .data(dtoList)
                .currentPage(pageData.getNumber())
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .build();
    }
}
