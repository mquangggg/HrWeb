package com.hrmanagement.hr_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// DTO để người dùng cập nhật thông tin cá nhân của chính mình
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Họ không được để trống")
    private String firstName;

    @NotBlank(message = "Tên không được để trống")
    private String lastName;

    private String phone;
}
