package com.example.employee_management_api.model;

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
    @Indexed(unique = true)
    private String employeeId;

    private String fullName;

    private String email;

    private Department department;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime lastModifiedAt;

    public Employee() {
    }

    public Employee(String employeeId, String fullName, String email, Department department, LocalDateTime createdAt, LocalDateTime lastModifiedAt) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
    }
}
