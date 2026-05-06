package com.hrmanagement.hr_management.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hrmanagement.hr_management.entity.Department;
import com.hrmanagement.hr_management.entity.Employee;
import com.hrmanagement.hr_management.entity.Position;
import com.hrmanagement.hr_management.enums.EmployeeStatus;
import com.hrmanagement.hr_management.enums.Role;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Check email
    Optional<Employee> findByEmail(String email);

    // Check email tồn tại
    boolean existsByEmail(String email);

    // Tìm kiếm linh hoạt theo từ khóa, phòng ban hoặc chức vụ
    @Query("SELECT e FROM Employee e WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:deptId IS NULL OR e.department.id = :deptId) " +
           "AND (:posId IS NULL OR e.position.id = :posId)")
    Page<Employee> searchEmployees(@Param("keyword") String keyword, 
                                   @Param("deptId") Long deptId, 
                                   @Param("posId") Long posId, 
                                   Pageable pageable);

    // Get all employees by status
    List<Employee> findByStatus(EmployeeStatus status);

    // Get all employees with a specific role
    Set<Employee> findByRole(Role role);

    // Get employees by department
    Set<Employee> findByDepartment(Department department);

    // Get employees by position
    Set<Employee> findByPosition(Position position);

    // Get employees by manager
    Set<Employee> findByManager(Employee manager);
}
