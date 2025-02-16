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
 * Provides methods to interact with the Employees collection in MongoDB.
 * It extends MongoRepository, enabling basic CRUD operations and query methods
 * </p>
 */
@Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {
    Employee findByEmployeeId(String employeeId);
    Employee findEmployeeByEmail(String email);

    @Query("{'$or': [ {'fullName': {$regex: ?0, $options: 'i'}}, {'department': {$regex: ?1, $options: 'i'}} ]}")
    Page<Employee> findByFullNameOrDepartment(String fullName, String department, Pageable pageable);
}
