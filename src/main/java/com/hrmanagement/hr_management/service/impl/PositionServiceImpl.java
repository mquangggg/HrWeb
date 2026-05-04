package com.hrmanagement.hr_management.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hrmanagement.hr_management.dto.request.PositionRequest;
import com.hrmanagement.hr_management.dto.response.PositionResponse;
import com.hrmanagement.hr_management.entity.Position;
import com.hrmanagement.hr_management.repository.PositionRepository;
import com.hrmanagement.hr_management.service.PositionService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {
    
    private final PositionRepository positionRepository;
    
    @Override
    public List<PositionResponse> getAllPositions() {
        return positionRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public PositionResponse getPositionById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ với id: " + id));
        return mapToResponse(position);
    }

    @Override
    public PositionResponse createPosition(PositionRequest request) {
        if (positionRepository.existsByName(request.getName())) {
            throw new RuntimeException("Chức vụ đã tồn tại");
        }
        Position position = new Position();
        position.setName(request.getName());
        position.setBaseSalary(request.getBaseSalary());
        return mapToResponse(positionRepository.save(position));
    }

    @Override
    public PositionResponse updatePosition(Long id, PositionRequest request) {
       Position position = positionRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ với id: " + id));
        if(!position.getName().toLowerCase().equalsIgnoreCase(request.getName().toLowerCase()) 
            && positionRepository.existsByName(request.getName())) {
            throw new RuntimeException("Chức vụ đã tồn tại");
        }
       position.setName(request.getName());
       position.setBaseSalary(request.getBaseSalary());
       return mapToResponse(positionRepository.save(position));
    }

    @Override
    public void deletePosition(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ với id: " + id));
        positionRepository.delete(position);
    }

    private PositionResponse mapToResponse(Position position) {
        return PositionResponse.builder()
                .id(position.getId())
                .name(position.getName())
                .baseSalary(position.getBaseSalary())
                .build();
    }
}
