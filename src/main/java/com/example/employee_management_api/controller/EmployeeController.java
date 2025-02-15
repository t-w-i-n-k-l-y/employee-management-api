package com.example.employee_management_api.controller;

import com.example.employee_management_api.dto.EmployeeDTO;
import com.example.employee_management_api.service.EmployeeService;
import com.example.employee_management_api.util.APIResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling employee-related HTTP requests.
 * Provides endpoints for CRUD operations on employees.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Creates a new employee object.
     *
     * @param employeeDTO the employee data transfer object to create, validated by @Valid
     * @return a ResponseEntity containing an ApiResponse with the created BlogPost object or a 500 status if creation fails
     */
    @PostMapping
    public ResponseEntity<APIResponse<EmployeeDTO>> createEmployee (@Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO savedEmployeeDTO = employeeService.createEmployee(employeeDTO);
        if (savedEmployeeDTO == null) {
            logger.error("Employee creation failed");
            return ResponseEntity.status(500).body(new APIResponse<>("Failed to create the employee", null, 500));
        }
        logger.info("New employee created with employeeId: {}", savedEmployeeDTO.getEmployeeId());
        return ResponseEntity.status(201).body(new APIResponse<>("Employee created successfully.", savedEmployeeDTO, 201));
    }
}
