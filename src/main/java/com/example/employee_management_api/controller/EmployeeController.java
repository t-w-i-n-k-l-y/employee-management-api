package com.example.employee_management_api.controller;

import com.example.employee_management_api.dto.EmployeeDTO;
import com.example.employee_management_api.service.EmployeeService;
import com.example.employee_management_api.util.APIResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * @return a ResponseEntity containing an ApiResponse with the created Employee object or a 500 status if creation fails
     */
    @PostMapping
    public ResponseEntity<APIResponse<EmployeeDTO>> createEmployee (@Valid @RequestBody EmployeeDTO employeeDTO) {
        logger.info("Received the request to create new employee");
        EmployeeDTO savedEmployeeDTO = employeeService.createEmployee(employeeDTO);
        if (savedEmployeeDTO == null) {
            logger.error("Employee creation failed");
            return ResponseEntity.status(500).body(new APIResponse<>("Failed to create the employee", null, 500));
        }
        logger.info("New employee created with employeeId: {}", savedEmployeeDTO.getEmployeeId());
        return ResponseEntity.status(201).body(new APIResponse<>("Employee created successfully.", savedEmployeeDTO, 201));
    }

    /**
     * Updates an existing employee object.
     *
     * @return a ResponseEntity containing an ApiResponse with the updated Employee object or a 404 status if employee not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<EmployeeDTO>> updateEmployee(@PathVariable String id, @Valid @RequestBody EmployeeDTO employeeDTO) {
        logger.info("Received request to update employee with ID: {}", id);

        EmployeeDTO updatedEmployeeDTO = employeeService.updateEmployee(id, employeeDTO);
        if (updatedEmployeeDTO == null) {
            logger.warn("Update failed or returned empty DTO for employee ID: {}", id);
            return ResponseEntity.status(500).body(new APIResponse<>("Employee update failed.", null, 500));
        }

        logger.info("Successfully updated employee with ID: {}", id);
        return ResponseEntity.status(200).body(new APIResponse<>("Employee details updated successfully.", updatedEmployeeDTO, 200));
    }

    /**
     * Deletes an employee by ID.
     *
     * @param id the ID of the employee to delete
     * @return a ResponseEntity containing an ApiResponse with the deleted Employee object or a 404 status if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<EmployeeDTO>> deleteEmployee(@PathVariable String id) {
        logger.info("Received request to delete employee with ID: {}", id);

        EmployeeDTO deletedEmployeeDTO = employeeService.deleteEmployee(id);
        if (deletedEmployeeDTO == null) {
            logger.error("Delete failed - Employee with ID {} not found.", id);
            return ResponseEntity.status(404).body(new APIResponse<>("Employee not found. Unable to delete.", null, 404));
        }
        return ResponseEntity.status(200).body(new APIResponse<>("Employee details deleted successfully.", deletedEmployeeDTO, 200));
    }

    /**
     * Find an employee by ID.
     *
     * @param id the _ID of the employee record in the mongodb
     * @return a ResponseEntity containing an ApiResponse with the Employee object or a 404 status if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<EmployeeDTO>> getEmployeeById (@PathVariable String id) {
        logger.info("Received request to find employee with _id: {}", id);

        EmployeeDTO employeeDTO = employeeService.getEmployeeById(id);
        if (employeeDTO == null) {
            logger.error("No employee found for the given _id: {}", id);
            return ResponseEntity.status(404).body(new APIResponse<>("Employee not found.", null, 404));
        }

        return ResponseEntity.status(200).body(new APIResponse<>("Employee details retrieved successfully", employeeDTO, 200));
    }

    /**
     * Find an employee by employee ID or get all employees.
     *
     * @param employeeId unique id of the employee (this is an optional query parameter)
     * @return a ResponseEntity containing an ApiResponse with the Employee object (query parameter is given)/ employee list or a 404 status if not found
     */
    @GetMapping()
    public ResponseEntity<APIResponse<?>> getAllEmployeesOrEmployeeByEmployeeId (@RequestParam(required = false) String employeeId, Pageable pageable) {

        if (employeeId != null) {
            logger.info("Received request to find employee with employee id: {}", employeeId);

            EmployeeDTO employeeDTO = employeeService.getEmployeeByEmployeeId(employeeId);
            if (employeeDTO == null) {
                logger.error("No employee found for the given employee id: {}", employeeId);
                return ResponseEntity.status(404).body(new APIResponse<>("Employee not found.", null, 404));
            }

            return ResponseEntity.status(200).body(new APIResponse<>("Employee details retrieved successfully", employeeDTO, 200));

        } else {
            logger.info("Received request to find all employees");
            List<EmployeeDTO> employees = employeeService.getAllEmployees(pageable);

            if (employees.isEmpty()) {
                return ResponseEntity.status(404).body(new APIResponse<>("No employees found.", employees, 404));
            }

            return ResponseEntity.status(200).body(new APIResponse<>("Employees retrieved successfully.", employees, 200));
        }
    }

    /**
     * Find an employees by employee name or department.
     *
     * @return a ResponseEntity containing an ApiResponse with the Employees having the given name or department or a 404 status if not found
     */
    @GetMapping("/search")
    public ResponseEntity<APIResponse<Object>> getEmployeesByFullNameOrDepartment(@RequestParam(required = false) String fullName, @RequestParam(required = false) String department, Pageable pageable) {

        List<EmployeeDTO> employeeDTOS = employeeService.getAllEmployeesByFullNameOrDepartment(fullName, department, pageable);
        if (employeeDTOS.isEmpty()) {
            logger.error("No matching employees exists for the name: {} or department: {}", fullName, department);
            return ResponseEntity.status(404).body(new APIResponse<>("No employees found.", null, 404));
        }

        logger.info("Retrieved employees successfully");
        return ResponseEntity.status(200).body(new APIResponse<>("Retrieved employees successfully", employeeDTOS, 200));

    }

}
