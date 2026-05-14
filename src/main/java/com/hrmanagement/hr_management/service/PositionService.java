package com.hrmanagement.hr_management.service;

import java.util.List;

import com.hrmanagement.hr_management.dto.request.PositionRequest;
import com.hrmanagement.hr_management.dto.response.PositionResponse;

public interface PositionService {
    List<PositionResponse> getAllPositions();
    PositionResponse getPositionById(Long id);
    PositionResponse createPosition(PositionRequest request);
    PositionResponse updatePosition(Long id, PositionRequest request);
    void deletePosition(Long id);
}
