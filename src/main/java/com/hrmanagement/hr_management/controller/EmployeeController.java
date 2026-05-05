package com.hrmanagement.hr_management.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrmanagement.hr_management.dto.request.EmployeeCreateRequest;
import com.hrmanagement.hr_management.dto.request.EmployeeUpdateRequest;
import com.hrmanagement.hr_management.dto.response.EmployeeResponse;
import com.hrmanagement.hr_management.dto.response.PageResponse;
import com.hrmanagement.hr_management.enums.EmployeeStatus;
import com.hrmanagement.hr_management.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // GET /api/v1/employees?page=0&size=10&keyword=abc
    // GET /api/v1/employees?page=0&size=10&keyword=abc&departmentId=1&positionId=2
    @GetMapping
    public ResponseEntity<PageResponse<EmployeeResponse>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long positionId) {
        return ResponseEntity.ok(employeeService.getAllEmployees(page, size, keyword, departmentId, positionId));
    }

    // GET /api/v1/employees/{id}
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    // POST /api/v1/employees
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(request));
    }

    // PUT /api/v1/employees/{id}
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    // PATCH /api/v1/employees/{id}/status?status=INACTIVE
    @PatchMapping("/{id}/status")
    public ResponseEntity<EmployeeResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam EmployeeStatus status) {
        return ResponseEntity.ok(employeeService.updateStatus(id, status));
    }

    // DELETE /api/v1/employees/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
