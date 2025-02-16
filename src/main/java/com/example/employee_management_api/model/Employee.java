package com.example.employee_management_api.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Mapped to a MongoDB collection and includes validation rules
 * for the fields using Jakarta Validation annotations.
 */
@Data
@Document(collection = "employees")
public class Employee {
    @Id
    private String id;

    @Indexed(unique = true)
    private String employeeId;

    @NotBlank(message = "{employee.name.blank}")
    @Size(min = 3, message = "{employee.name.size}")
    private String fullName;

    @NotBlank(message = "{employee.email.blank}")
    @Email(message = "{employee.email.format}")
    private String email;

    @NotBlank(message = "{employee.department.blank}")
    private Department department;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime lastModifiedAt;

    public Employee() {
    }

    public Employee(String id, String employeeId, String fullName, String email, Department department, LocalDateTime createdAt, LocalDateTime lastModifiedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
    }
}
