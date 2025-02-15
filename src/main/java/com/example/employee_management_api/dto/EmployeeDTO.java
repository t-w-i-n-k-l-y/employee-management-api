package com.example.employee_management_api.dto;

import com.example.employee_management_api.model.Department;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for Employee.
 * Used for transferring employee data while avoiding direct exposure of entity fields.
 * This DTO excludes database metadata fields like id, createdAt, and lastModifiedAt.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {
    @NotBlank(message = "{employee.employeeId.blank}")
    private String employeeId;

    @NotBlank(message = "{employee.name.blank}")
    @Size(min = 3, message = "{employee.name.size}")
    private String fullName;

    @NotBlank(message = "{employee.email.blank}")
    @Email(message = "{employee.email.format}")
    private String email;

    @NotBlank(message = "{employee.department.blank}")
    private Department department;
}
