package com.example.employee_management_api.repository;

import com.example.employee_management_api.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Employee entity
 * <p>
 * This interface provides methods to interact with the Employee collection in MongoDB.
 * It extends MongoRepository, enabling basic CRUD operations.
 * </p>
 *
 * <p><b>Additional Methods:</b></p>
 * <ul>
 *     <li>Find an employee by their unique employee ID.</li>
 *     <li>Search employees by name or department.</li>
 * </ul>
 */
@Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {
    Employee findByEmployeeId(String employeeId);
    Employee findEmployeeByEmail(String email);

    @Query("{'$or': [ {'fullName': {$regex: ?0, $options: 'i'}}, {'department': {$regex: ?1, $options: 'i'}} ]}")
    Page<Employee> findByFullNameOrDepartment(String fullName, String department, Pageable pageable);
    Page<Employee> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);
    Page<Employee> findByDepartmentContainingIgnoreCase(String department, Pageable pageable);
}
