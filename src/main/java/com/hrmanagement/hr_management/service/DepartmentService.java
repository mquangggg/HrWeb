package com.hrmanagement.hr_management.service;

import java.util.List;

import com.hrmanagement.hr_management.dto.request.DepartmentRequest;
import com.hrmanagement.hr_management.dto.response.DepartmentResponse;

public interface DepartmentService {
    List<DepartmentResponse> getAllDepartments();
    DepartmentResponse getDepartmentById(Long id);
    DepartmentResponse createDepartment(DepartmentRequest request);
    DepartmentResponse updateDepartment(Long id, DepartmentRequest request);
    void deleteDepartment(Long id);
}
