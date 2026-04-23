package com.hrmanagement.hr_management.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrmanagement.hr_management.entity.Department;
import com.hrmanagement.hr_management.entity.Employee;
import com.hrmanagement.hr_management.entity.Position;
import com.hrmanagement.hr_management.enums.EmployeeStatus;
import com.hrmanagement.hr_management.enums.Role;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Check email
    Optional<Employee> findByEmail(String email);
    
    // Get all active employees
    Set<Employee> findByStatus(EmployeeStatus status);

    // Get all employees with a specific role
    Set<Employee> findByRole(Role role);

    // Get employees by department
    Set<Employee> findByDepartment(Department department);

    // Get employees by position
    Set<Employee> findByPosition(Position position);
    
    // Get employees by manager
    Set<Employee> findByManager(Employee manager);
    
}
