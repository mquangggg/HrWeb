package com.hrmanagement.hr_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AuthResponse {
    // Dùng để trả về dữ liệu auth cho client

    private String token;

    @Builder.Default
    private String tokenType = "Bearer";

    private EmployeeResponse user; // Trả về thông tin user vừa đăng nhập
}
