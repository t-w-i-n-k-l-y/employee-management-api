package com.example.employee_management_api.service;

import com.example.employee_management_api.dto.EmployeeDTO;
import com.example.employee_management_api.exception.DuplicateValueException;
import com.example.employee_management_api.exception.EmployeeAlreadyExistsException;
import com.example.employee_management_api.exception.NoSuchEmployeeExistsException;
import com.example.employee_management_api.exception.ResourceNotFoundException;
import com.example.employee_management_api.model.Employee;
import com.example.employee_management_api.repository.EmployeeRepository;
import com.example.employee_management_api.dto.APIResponse;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing employees.
 * Handles business logic and interacts with the EmployeeRepository.
 */
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final CounterService counterService;
    private final ModelMapper modelMapper;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, CounterService counterService, ModelMapper modelMapper) {
        this.employeeRepository = employeeRepository;
        this.counterService = counterService;
        this.modelMapper = modelMapper;
    }

    /**
     * Creates a new employee.
     */
    @Transactional
    public APIResponse<EmployeeDTO> createEmployee(EmployeeDTO employeeDTO) {
        logger.info("Attempting to create a new employee with email: {}", employeeDTO.getEmail());

        if (employeeRepository.findEmployeeByEmail(employeeDTO.getEmail()) != null) {
            logger.error("Employee creation failed: Email {} already exists", employeeDTO.getEmail());
            throw new EmployeeAlreadyExistsException("An employee with the email " + employeeDTO.getEmail() + "already exists.");
        }

        try {
            // Getting next sequence for unique employee id
            int nextSequence = counterService.getNextSequence("employeeId");

            // Creating the employeeID
            String nextId = String.format("EM%04d", nextSequence);
            logger.info("Generated unique Employee ID: {}", nextId);
            employeeDTO.setEmployeeId(nextId);

            Employee employeeToBeSaved = modelMapper.map(employeeDTO, Employee.class);
            employeeToBeSaved.setCreatedAt(LocalDateTime.now());

            Employee savedEmployee = employeeRepository.save(employeeToBeSaved);
            EmployeeDTO savedEmployeeDTO = modelMapper.map(savedEmployee, EmployeeDTO.class);
            if (savedEmployeeDTO == null) {
                logger.error("Employee creation failed");
                return new APIResponse<>("Failed to create the employee", null, 500);
            }
            logger.info("New employee created with employeeId: {}", savedEmployeeDTO.getEmployeeId());
            return new APIResponse<>("Employee created successfully.", savedEmployeeDTO, 201);

        } catch (Exception e) {
            logger.error("Unexpected error occurred while creating employee with email {}: {}", employeeDTO.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to create employee", e);
        }
    }

    /**
     * Update an existing employee.
     */
    @Transactional
    public APIResponse<EmployeeDTO> updateEmployee(String employeeId, EmployeeDTO updatedEmployeeDTO) {
        logger.info("Attempting to update employee with ID: {}", employeeId);

        Employee existingEmployee = employeeRepository.findByEmployeeId(employeeId);
        if(existingEmployee == null) {
            logger.error("Requested employee not found with ID: {}", employeeId);
            throw new NoSuchEmployeeExistsException("Employee not found with ID: " + employeeId);
        }

        // Assigning only the fields need to be updated
        if(updatedEmployeeDTO.getFullName() != null && !updatedEmployeeDTO.getFullName().isEmpty()) {
            existingEmployee.setFullName(updatedEmployeeDTO.getFullName());
        }
        if(updatedEmployeeDTO.getEmail() != null && !updatedEmployeeDTO.getEmail().isEmpty() && !existingEmployee.getEmail().equals(updatedEmployeeDTO.getEmail())) {
            if (employeeRepository.findEmployeeByEmail(updatedEmployeeDTO.getEmail()) != null) {
                logger.error("Employee update failed: Email {} already exists", updatedEmployeeDTO.getEmail());
                throw new DuplicateValueException("An employee already exists with the email: " + updatedEmployeeDTO.getEmail());
            }
            existingEmployee.setEmail(updatedEmployeeDTO.getEmail());
        }
        if(updatedEmployeeDTO.getDepartment() != null) {
            existingEmployee.setDepartment(updatedEmployeeDTO.getDepartment());
        }

        try{
            Employee savedEmployee = employeeRepository.save(existingEmployee);
            logger.info("Employee updated successfully: {}", savedEmployee);
            EmployeeDTO savedEmployeeDTO = modelMapper.map(savedEmployee, EmployeeDTO.class);
            if (savedEmployeeDTO == null) {
                logger.warn("Update failed or returned empty DTO for employee ID: {}", employeeId);
                throw new RuntimeException("Failed to update the employee");
            }

            logger.info("Successfully updated employee with ID: {}", employeeId);
            return new APIResponse<>("Employee details updated successfully.", savedEmployeeDTO, 200);

        } catch (DataAccessException e) {
            logger.error("Database error while updating employee with ID: {}", employeeId, e);
            throw new DataAccessResourceFailureException("Failed to update employee. Please try again later.");
        } catch (Exception e) {
            logger.error("Unexpected error while updating employee with ID: {}", employeeId, e);
            throw new RuntimeException("An unexpected error occurred.");
        }
    }

    /**
     * Delete an existing employee.
     */
    public APIResponse<String> deleteEmployee(String employeeId) {
        logger.info("Deleting employee with ID: {}", employeeId);

        Employee existingEmployee = employeeRepository.findByEmployeeId(employeeId);
        if(existingEmployee == null) {
            logger.error("Employee not found with ID: {}", employeeId);
            throw new NoSuchEmployeeExistsException("Employee not found with ID: " + employeeId);
        }

        try {
            employeeRepository.delete(existingEmployee);
            logger.info("Successfully deleted employee with ID: {}", employeeId);

            return new APIResponse<>("Employee with id: " + employeeId + " deleted successfully.", "Employee ID: " + employeeId, 200);

        } catch (DataAccessException e) {
            logger.error("Database error while deleting employee with ID: {}", employeeId, e);
            throw new DataAccessResourceFailureException("Database error occurred while deleting employee.");

        } catch (Exception e) {
            logger.error("Unexpected error occurred while deleting employee with ID: {}", employeeId, e);
            throw new RuntimeException("An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Get an existing employee by employee ID.
     */
    public APIResponse<EmployeeDTO> getEmployeeByEmployeeId (String employeeId) {
        logger.info("Getting employee details for the employee id: {}", employeeId);
        try {
            Employee employee = employeeRepository.findByEmployeeId(employeeId);
            if (employee == null) {
                logger.error("Cannot find the employee for the given employee id: {}", employeeId);
                throw new NoSuchEmployeeExistsException("No Employee found for the given id: " + employeeId);
            }
            logger.info("Successfully retrieved employee details for the employee id: {}", employeeId);
            return new APIResponse<>("Employee details retrieved successfully", modelMapper.map(employee, EmployeeDTO.class), 200);

        } catch (Exception e) {
            logger.error("An error occurred while retrieving employee by employee id.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Get all employees
     */
    public APIResponse<List<EmployeeDTO>> getAllEmployees(Pageable pageable) {
        logger.info("Fetching all employees from the database");

        try {
            Page<Employee> employees = employeeRepository.findAll(pageable);

            if (employees.isEmpty()) {
                logger.warn("No employees found in the database.");
                throw new ResourceNotFoundException("No employees found in the database");
            }

            List<EmployeeDTO> employeeDTOSList = employees.stream()
                    .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                    .toList();

            logger.info("Successfully retrieved {} employees.", employeeDTOSList.size());

            return new APIResponse<>("Employees retrieved successfully.", employeeDTOSList, 200);

        } catch (DataAccessException e) {
            logger.error("Database error while fetching employees.", e);
            throw new RuntimeException("Database error occurred while retrieving employees. Please try again later.");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching employees.", e);
            throw new RuntimeException("An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Get all employees by name or department
     */
    public APIResponse<List<EmployeeDTO>> getAllEmployeesByFullNameOrDepartment(String fullName, String department, Pageable pageable) {
        logger.info("Fetching all employees from the database matches name or department");

        Page<Employee> employees;
        try {
            // Handle fullName or department null scenarios for the regex
            String fullNameQuery = (fullName == null || fullName.isEmpty()) ? "" : fullName;
            String departmentQuery = (department == null || department.isEmpty()) ? "" : department;

            employees = employeeRepository.findByFullNameOrDepartment(fullNameQuery, departmentQuery, pageable);

            if (employees.isEmpty()) {
                logger.warn("No employees found with similar name or department");
                throw new ResourceNotFoundException("No employees found in the database");
            }

            logger.info("Successfully retrieved {} employees for given name or department", employees.getSize());
            List<EmployeeDTO> employeeDTOSList = employees.stream()
                    .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                    .toList();
            return new APIResponse<>("Retrieved employees successfully", employeeDTOSList, 200);

        } catch (DataAccessException e) {
            logger.error("Database error while fetching employees.", e);
            throw new RuntimeException("Database error occurred while retrieving employees. Please try again later.");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching employees.", e);
            throw new RuntimeException("An unexpected error occurred. Please try again later.");
        }
    }
}