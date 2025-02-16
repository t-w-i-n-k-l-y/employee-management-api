package com.example.employee_management_api.service;

import com.example.employee_management_api.dto.EmployeeDTO;
import com.example.employee_management_api.exception.DuplicateValueException;
import com.example.employee_management_api.mapper.EmployeeMapper;
import com.example.employee_management_api.model.Employee;
import com.example.employee_management_api.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.Optional;

/**
 * Service class for managing employees.
 * Handles business logic and interacts with the EmployeeRepository.
 */
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final CounterService counterService;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, CounterService counterService) {
        this.employeeRepository = employeeRepository;
        this.counterService = counterService;
    }

    /**
     * Creates a new employee.
     *
     * @param employeeDTO the employee data transfer object to create
     * @return the saved employee
     */
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        logger.info("Attempting to create a new employee with email: {}", employeeDTO.getEmail());

        if (employeeRepository.findEmployeeByEmail(employeeDTO.getEmail()) != null) {
            logger.error("Employee creation failed: Email {} already exists", employeeDTO.getEmail());
            throw new DuplicateValueException("Employee email already exists");
        }

        try {
            // Getting next sequence for unique employee id
            int nextSequence = counterService.getNextSequence("employeeId");

            // Creating the employeeID
            String nextId = String.format("EM%04d", nextSequence);
            logger.info("Generated unique Employee ID: {}", nextId);
            employeeDTO.setEmployeeId(nextId);

            Employee savedEmployee = employeeRepository.save(EmployeeMapper.toEntity(employeeDTO, null));
            return EmployeeMapper.toDTO(savedEmployee);

        } catch (Exception e) {
            logger.error("Unexpected error occurred while creating employee with email {}: {}", employeeDTO.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to create employee", e);
        }
    }

    /**
     * Update an existing employee.
     *
     * @return the saved employee
     */
    public EmployeeDTO updateEmployee(String employeeId, EmployeeDTO updatedEmployeeDTO) {
        logger.info("Updating employee with ID: {}", employeeId);

        Employee existingEmployee = employeeRepository.findByEmployeeId(employeeId);
        if(existingEmployee == null) {
            logger.error("Employee not found with ID: {}", employeeId);
            throw new ResourceAccessException("Employee not found with ID: " + employeeId);
        }

        logger.debug("Existing employee details: {}", existingEmployee);

        if(updatedEmployeeDTO.getFullName() != null) {
            existingEmployee.setFullName(updatedEmployeeDTO.getFullName());
        }
        if(updatedEmployeeDTO.getEmail() != null) {
            existingEmployee.setEmail(updatedEmployeeDTO.getEmail());
        }
        if(updatedEmployeeDTO.getDepartment() != null) {
            existingEmployee.setDepartment(updatedEmployeeDTO.getDepartment());
        }

        try{
            Employee savedEmployee = employeeRepository.save(existingEmployee);
            logger.info("Employee updated successfully: {}", savedEmployee);
            return EmployeeMapper.toDTO(savedEmployee);
        } catch (DataAccessException e) {
            logger.error("Database error while updating employee with ID: {}", employeeId, e);
            throw new RuntimeException("Failed to update employee. Please try again later.");
        } catch (Exception e) {
            logger.error("Unexpected error while updating employee with ID: {}", employeeId, e);
            throw new RuntimeException("An unexpected error occurred.");
        }
    }
}
