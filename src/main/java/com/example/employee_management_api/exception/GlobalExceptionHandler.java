package com.example.employee_management_api.exception;

import com.example.employee_management_api.dto.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==========================================================
    // VALIDATION & CLIENT ERRORS (400 - BAD REQUEST)
    // ==========================================================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIResponse<String>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("Invalid argument received: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new APIResponse<>("Found invalid details in the request. Check and try again.", null, 400));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        // Extract field names and their validation messages
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        logger.warn("Validation error: {}", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new APIResponse<>("Request failed because of invalid details", errors, 400));
    }

    // ==========================================================
    // RESOURCE NOT FOUND ERRORS (404 - NOT FOUND)
    // ==========================================================
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse<String>> handleResourceNotFoundException(ResourceNotFoundException e) {
        logger.error("Resource not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new APIResponse<>(e.getMessage(), null, 404));
    }

    @ExceptionHandler(NoSuchEmployeeExistsException.class)
    public  ResponseEntity<APIResponse<String>> handleNoSuchEmployeeExistsException (NoSuchEmployeeExistsException e) {
        logger.error("Employee not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new APIResponse<>(e.getMessage(), null, 404));
    }

    // ==========================================================
    // CONFLICT ERRORS (409 - DUPLICATE / EXISTING RESOURCE)
    // ==========================================================
    @ExceptionHandler(DuplicateValueException.class)
    public ResponseEntity<APIResponse<String>> handleDuplicateValueException(DuplicateValueException e) {
        logger.warn("Duplicate value error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new APIResponse<>(e.getMessage(), null, 409));
    }

    @ExceptionHandler(EmployeeAlreadyExistsException.class)
    public  ResponseEntity<APIResponse<String>> handleEmployeeAlreadyExistsException (EmployeeAlreadyExistsException e) {
        logger.warn("Attempt to create duplicate employee: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new APIResponse<>(e.getMessage(), null, 409));
    }

    // ==========================================================
    // DATABASE ERRORS (500 - INTERNAL SERVER ERROR)
    // ==========================================================
    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<APIResponse<String>> handleDataAccessResourceFailureException (DataAccessResourceFailureException e) {
        logger.error("Database access error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new APIResponse<>("There is an issue with accessing the data. Try again later.", null, 500));
    }


    // ==========================================================
    // GENERAL SERVER ERRORS (500 - INTERNAL SERVER ERROR)
    // ==========================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<String>> handleGeneralException(Exception e) {
        logger.error("Unexpected service error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new APIResponse<>("There was an issue with processing your request. Please try again later.", null, 500));
    }
}
