package com.example.employee_management_api.exception;

/**
 * Custom exception when a requested resource is not found.
 */
public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
