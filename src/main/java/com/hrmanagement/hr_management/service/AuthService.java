package com.hrmanagement.hr_management.service;

import com.hrmanagement.hr_management.dto.request.LoginRequest;
import com.hrmanagement.hr_management.dto.request.RegisterRequest;
import com.hrmanagement.hr_management.dto.response.AuthResponse;
import com.hrmanagement.hr_management.dto.response.EmployeeResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    
    EmployeeResponse getMe();
}
