package com.hrmanagement.hr_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {
    // Dùng để trả về dữ liệu department cho client
    private Long id;
    private String name;
    private Long managerId;
    private String managerName;
}
