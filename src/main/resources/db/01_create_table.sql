USE db_hr_management;
Create table positions(
	id BIGINT AUTO_INCREMENT primary KEY,
    name varchar(100) not null unique,
    base_salary DECIMAL(15,2));
    
create table departments(
	id BIGINT AUTO_INCREMENT primary KEY,
    name varchar(100) not null unique,
    description TEXT,
    manager_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    department_id BIGINT,
    position_id BIGINT,
    role ENUM('ADMIN', 'MANAGER', 'EMPLOYEE') NOT NULL,
    base_salary DECIMAL(15,2) DEFAULT 0,
    allowance DECIMAL(15,2) DEFAULT 0,
    start_date DATE,
    status ENUM('active', 'inactive') DEFAULT 'active',
    CONSTRAINT fk_emp_dept FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_emp_pos FOREIGN KEY (position_id) REFERENCES positions(id)
);
ALTER TABLE departments 
ADD CONSTRAINT fk_dept_manager FOREIGN KEY (manager_id) REFERENCES employees(id);
CREATE TABLE attendances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT,
    date DATE NOT NULL,
    check_in TIME,
    check_out TIME,
    status ENUM('present', 'absent', 'late') DEFAULT 'present',
    CONSTRAINT fk_atten_emp FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE leave_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT NOT NULL,
    status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    approved_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_leave_emp FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_leave_approver FOREIGN KEY (approved_by) REFERENCES employees(id)
);

CREATE TABLE payrolls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT,
    month INT NOT NULL CHECK (month BETWEEN 1 AND 12),
    year INT NOT NULL,
    working_days INT,
    absent_days INT DEFAULT 0,
    base_salary DECIMAL(15,2),
    allowance DECIMAL(15,2) DEFAULT 0,
    deduction DECIMAL(15,2) DEFAULT 0,
    net_salary DECIMAL(15,2),
    CONSTRAINT fk_pay_emp FOREIGN KEY (employee_id) REFERENCES employees(id)
);


    