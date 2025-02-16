package com.example.employee_management_api.service;

import com.example.employee_management_api.dto.EmployeeDTO;
import com.example.employee_management_api.exception.DuplicateValueException;
import com.example.employee_management_api.exception.ResourceNotFoundException;
import com.example.employee_management_api.model.Employee;
import com.example.employee_management_api.repository.EmployeeRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, CounterService counterService, ModelMapper modelMapper) {
        this.employeeRepository = employeeRepository;
        this.counterService = counterService;
        this.modelMapper = modelMapper;
    }

    /**
     * Creates a new employee.
     */
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        logger.info("Attempting to create a new employee with email: {}", employeeDTO.getEmail());

        if (employeeDTO.getFullName().isEmpty() || employeeDTO.getEmail().isEmpty() || employeeDTO.getDepartment().toString().isEmpty()) {
            logger.error("Employee creation failed: Full name, email and department are required");
            throw new IllegalArgumentException("Full name, email, and department cannot be empty");
        }

        if (!Pattern.matches(EMAIL_REGEX, employeeDTO.getEmail())) {
            logger.error("Employee creation failed: invalid email");
            throw new IllegalArgumentException("Invalid email format: " + employeeDTO.getEmail());
        }

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

            Employee employeeToBeSaved = modelMapper.map(employeeDTO, Employee.class);
            employeeToBeSaved.setCreatedAt(LocalDateTime.now());

            Employee savedEmployee = employeeRepository.save(employeeToBeSaved);
            return modelMapper.map(savedEmployee, EmployeeDTO.class);

        } catch (Exception e) {
            logger.error("Unexpected error occurred while creating employee with email {}: {}", employeeDTO.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to create employee", e);
        }
    }

    /**
     * Update an existing employee.
     */
    public EmployeeDTO updateEmployee(String employeeId, EmployeeDTO updatedEmployeeDTO) {
        logger.info("Updating employee with ID: {}", employeeId);

        Employee existingEmployee = employeeRepository.findByEmployeeId(employeeId);
        if(existingEmployee == null) {
            logger.error("Requested employee not found with ID: {}", employeeId);
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
            return modelMapper.map(savedEmployee, EmployeeDTO.class);
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
    public EmployeeDTO deleteEmployee(String id) {
        logger.info("Deleting employee with ID: {}", id);

        Employee existingEmployee = employeeRepository.findByEmployeeId(id);
        if(existingEmployee == null) {
            logger.error("Employee not found with ID: {}", id);
            throw new ResourceNotFoundException("Employee not found with ID: " + id);
        }

        try {
            employeeRepository.delete(existingEmployee);
            logger.info("Successfully deleted employee with ID: {}", id);

            return modelMapper.map(existingEmployee, EmployeeDTO.class);

        } catch (DataAccessException e) {
            logger.error("Database error while deleting employee with ID: {}", id, e);
            throw new DataAccessResourceFailureException("Database error occurred while deleting employee.");

        } catch (Exception e) {
            logger.error("Unexpected error occurred while deleting employee with ID: {}", id, e);
            throw new RuntimeException("An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Get an existing employee by mongoDB ID.
     */
    public EmployeeDTO getEmployeeById (String id) {
        logger.info("Getting employee details for the _id: {}", id);
        try {
            Employee employee = employeeRepository.findById(id).orElse(null);
            if (employee == null) {
                logger.error("Cannot find the employee for the given _id: {}", id);
                throw new ResourceNotFoundException("No Employee found for the given _id: " + id);
            }
            logger.info("Successfully retrieved employee details for the _id: {}", id);
            return modelMapper.map(employee, EmployeeDTO.class);
        } catch (ResourceNotFoundException e) {
            logger.error("No employee found for the given _id.");
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("An error occurred while retrieving employee by _id.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Get an existing employee by employee ID.
     */
    public EmployeeDTO getEmployeeByEmployeeId (String employeeId) {
        logger.info("Getting employee details for the employee id: {}", employeeId);
        try {
            Employee employee = employeeRepository.findByEmployeeId(employeeId);
            if (employee == null) {
                logger.error("Cannot find the employee for the given employee id: {}", employeeId);
                throw new ResourceNotFoundException("No Employee found for the given id: " + employeeId);
            }
            logger.info("Successfully retrieved employee details for the employee id: {}", employeeId);
            return modelMapper.map(employee, EmployeeDTO.class);
        } catch (ResourceNotFoundException e) {
            logger.error("No employee found for the given employee id.");
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("An error occurred while retrieving employee by employee id.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Get all employees
     */
    public List<EmployeeDTO> getAllEmployees(Pageable pageable) {
        logger.info("Fetching all employees from the database");

        try {
            Page<Employee> employees = employeeRepository.findAll(pageable);

            if (employees.isEmpty()) {
                logger.warn("No employees found in the database.");
                throw new ResourceNotFoundException("No employees found in the database");
            }

            logger.info("Successfully retrieved {} employees.", employees.getSize());

            return employees.stream()
                    .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                    .collect(Collectors.toList());

        } catch(ResourceNotFoundException e) {
            throw new ResourceNotFoundException(e.getMessage());
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
    public List<EmployeeDTO> getAllEmployeesByFullNameOrDepartment(String fullName, String department, Pageable pageable) {
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

            return employees.stream()
                    .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                    .collect(Collectors.toList());

        } catch(ResourceNotFoundException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (DataAccessException e) {
            logger.error("Database error while fetching employees.", e);
            throw new RuntimeException("Database error occurred while retrieving employees. Please try again later.");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching employees.", e);
            throw new RuntimeException("An unexpected error occurred. Please try again later.");
        }
    }

}
