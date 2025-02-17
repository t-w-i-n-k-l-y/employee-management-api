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
     * Validated the request body by using @Valid
     *
     * @return a ResponseEntity containing an ApiResponse with the created Employee object or a 500 or 400 status if creation fails
     */
    @PostMapping
    public ResponseEntity<APIResponse<EmployeeDTO>> createEmployee (@Valid @RequestBody EmployeeDTO employeeDTO) {
        logger.info("Received the request to create new employee");
        APIResponse<EmployeeDTO> apiResponse = employeeService.createEmployee(employeeDTO);
        return ResponseEntity.status(apiResponse.getStatusCode()).body(apiResponse);
    }

    /**
     * Updates an existing employee object.
     *
     * @param id  employee ID of the employee to be updated
     * @return a ResponseEntity containing an ApiResponse with the updated Employee object or a 404 status if employee not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<EmployeeDTO>> updateEmployee(@PathVariable String id, @Valid @RequestBody EmployeeDTO employeeDTO) {
        logger.info("Received request to update employee with ID: {}", id);
        APIResponse<EmployeeDTO> apiResponse = employeeService.updateEmployee(id, employeeDTO);
        return ResponseEntity.status(apiResponse.getStatusCode()).body(apiResponse);
    }

    /**
     * Deletes an employee by ID.
     *
     * @param id  employee ID of the employee to delete
     * @return a ResponseEntity containing an ApiResponse with the deleted Employee object or a 404 status if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<EmployeeDTO>> deleteEmployee(@PathVariable String id) {
        logger.info("Received request to delete employee with ID: {}", id);
        APIResponse<EmployeeDTO> apiResponse = employeeService.deleteEmployee(id);
        return ResponseEntity.status(apiResponse.getStatusCode()).body(apiResponse);
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
        APIResponse<EmployeeDTO> apiResponse = employeeService.getEmployeeById(id);
        return ResponseEntity.status(apiResponse.getStatusCode()).body(apiResponse);
    }

    /**
     * Find an employee by employee ID or get all employees.
     *
     * @return a ResponseEntity containing an ApiResponse with the Employee object (query parameter is given)/ employee list or a 404 status if not found
     */
    @GetMapping()
    public ResponseEntity<APIResponse<?>> getAllEmployeesOrEmployeeByEmployeeId (@RequestParam(required = false) String employeeId, Pageable pageable) {

        if (employeeId != null) {
            logger.info("Received request to find employee with employee id: {}", employeeId);
            APIResponse<EmployeeDTO> apiResponse = employeeService.getEmployeeByEmployeeId(employeeId);
            return ResponseEntity.status(apiResponse.getStatusCode()).body(apiResponse);

        } else {
            logger.info("Received request to find all employees");
            APIResponse<List<EmployeeDTO>> apiResponse = employeeService.getAllEmployees(pageable);
            return ResponseEntity.status(apiResponse.getStatusCode()).body(apiResponse);
        }
    }

    /**
     * Find an employees by employee name or department.
     *
     * @return a ResponseEntity containing an ApiResponse with the Employees having the given name or department or a 404 status if not found
     */
    @GetMapping("/search")
    public ResponseEntity<APIResponse<List<EmployeeDTO>>> getEmployeesByFullNameOrDepartment(@RequestParam(required = false) String fullName, @RequestParam(required = false) String department, Pageable pageable) {
        logger.info("Received request to search all employees with name: {} and department: {}", fullName, department);
        APIResponse<List<EmployeeDTO>> apiResponse = employeeService.getAllEmployeesByFullNameOrDepartment(fullName, department, pageable);
        return ResponseEntity.status(apiResponse.getStatusCode()).body(apiResponse);
    }

}
