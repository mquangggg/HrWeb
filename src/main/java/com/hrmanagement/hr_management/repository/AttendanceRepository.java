package com.hrmanagement.hr_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hrmanagement.hr_management.entity.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

}
