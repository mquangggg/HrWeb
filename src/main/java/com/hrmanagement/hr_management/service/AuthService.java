package com.hrmanagement.hr_management.service;

import com.hrmanagement.hr_management.dto.request.ChangePasswordRequest;
import com.hrmanagement.hr_management.dto.request.LoginRequest;
import com.hrmanagement.hr_management.dto.request.RegisterRequest;
import com.hrmanagement.hr_management.dto.request.UpdateProfileRequest;
import com.hrmanagement.hr_management.dto.response.AuthResponse;
import com.hrmanagement.hr_management.dto.response.EmployeeResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);

    // Lấy thông tin người dùng hiện tại
    EmployeeResponse getMe();

    // Cập nhật thông tin cá nhân (họ, tên, SĐT)
    EmployeeResponse updateProfile(UpdateProfileRequest request);

    // Đổi mật khẩu (cần nhập mật khẩu cũ để xác minh)
    void changePassword(ChangePasswordRequest request);
}
