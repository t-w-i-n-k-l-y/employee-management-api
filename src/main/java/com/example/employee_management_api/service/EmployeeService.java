package com.example.employee_management_api.service;

import com.example.employee_management_api.dto.EmployeeDTO;
import com.example.employee_management_api.exception.DuplicateValueException;
import com.example.employee_management_api.exception.ResourceNotFoundException;
import com.example.employee_management_api.mapper.EmployeeMapper;
import com.example.employee_management_api.model.Employee;
import com.example.employee_management_api.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.stream.Collectors;

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
            return EmployeeMapper.toDTO(savedEmployee);
        } catch (DataAccessException e) {
            logger.error("Database error while updating employee with ID: {}", employeeId, e);
            throw new RuntimeException("Failed to update employee. Please try again later.");
        } catch (Exception e) {
            logger.error("Unexpected error while updating employee with ID: {}", employeeId, e);
            throw new RuntimeException("An unexpected error occurred.");
        }
    }

    /**
     * Delete an existing employee.
     *
     * @return the deleted employee
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

            return EmployeeMapper.toDTO(existingEmployee);

        } catch (DataAccessException e) {
            logger.error("Database error while deleting employee with ID: {}", id, e);
            throw new RuntimeException("Database error occurred while deleting employee.");

        } catch (Exception e) {
            logger.error("Unexpected error occurred while deleting employee with ID: {}", id, e);
            throw new RuntimeException("An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Get an existing employee by mongoDB ID.
     *
     * @return the employee if exists or else return an error
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
            return EmployeeMapper.toDTO(employee);
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
     *
     * @return the employee if exists or else return an error
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
            return EmployeeMapper.toDTO(employee);
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
     *
     * @return all existing employees
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
                    .map(EmployeeMapper::toDTO)
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
     *
     * @return all existing employees that matches the given name or department
     */
    public List<EmployeeDTO> getAllEmployeesByFullNameOrDepartment(String fullName, String department, Pageable pageable) {
        logger.info("Fetching all employees from the database matches name or department");

        Page<Employee> employees;
        try {
            if (fullName == null && department == null) {
                logger.error("At least one parameter (name or department) must be provided.");
                throw new IllegalArgumentException("At least one parameter (name or department) must be provided.");
            }
            if(fullName != null && department != null) {
                employees = employeeRepository.findByFullNameOrDepartment(fullName, department, pageable);
            } else if (fullName != null) {
                employees = employeeRepository.findByFullNameContainingIgnoreCase(fullName, pageable);
            } else {
                employees = employeeRepository.findByDepartmentContainingIgnoreCase(department, pageable);
            }

            if (employees.isEmpty()) {
                logger.warn("No employees found with similar name or department");
                throw new ResourceNotFoundException("No employees found in the database");
            }

            logger.info("Successfully retrieved {} employees for given name or department", employees.getSize());

            return employees.stream()
                    .map(EmployeeMapper::toDTO)
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
