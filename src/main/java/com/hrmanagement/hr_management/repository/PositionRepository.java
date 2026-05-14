package com.hrmanagement.hr_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hrmanagement.hr_management.entity.Position;

public interface PositionRepository extends JpaRepository<Position, Long> {
    boolean existsByName(String name);
}
