package com.example.employee_management_api.dto;

import com.example.employee_management_api.model.Department;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for Employee.
 * For transferring employee data avoiding direct exposure of entity fields.
 * This excludes database metadata fields like id, createdAt, and lastModifiedAt.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {
    private String employeeId;

    @NotBlank(message = "{employee.name.blank}")
    @Size(min = 3, message = "{employee.name.size}")
    private String fullName;

    @NotBlank(message = "{employee.email.blank}")
    @Email(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "{employee.email.format}")
    private String email;

    @NotNull(message = "{employee.department.blank}")
    private Department department;
}
