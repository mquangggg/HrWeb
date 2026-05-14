package com.hrmanagement.hr_management.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.hrmanagement.hr_management.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByName(String name);
}
