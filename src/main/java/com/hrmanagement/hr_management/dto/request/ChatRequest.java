package com.hrmanagement.hr_management.dto.request;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private List<Map<String, String>> history;
}
