package com.example.employee_management_api.exception;

import com.example.employee_management_api.util.APIResponse;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the application.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIResponse<String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new APIResponse<>(e.getMessage(), null, 400));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<String>> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new APIResponse<>("Something went wrong!", null, 500));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<APIResponse<String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new APIResponse<>("Unexpected error occurred!", null, 500));
    }

    @ExceptionHandler(DuplicateValueException.class)
    public ResponseEntity<APIResponse<String>> handleDuplicateValueException(DuplicateValueException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new APIResponse<>(e.getMessage(), null, 409));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse<String>> handleResourceNotFoundException(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new APIResponse<>(e.getMessage(), null, 404));
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<APIResponse<String>> handleDataAccessResourceFailureException (DataAccessResourceFailureException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new APIResponse<>(e.getMessage(), null, 500));
    }
}
