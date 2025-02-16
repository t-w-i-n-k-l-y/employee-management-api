package com.example.employee_management_api.exception;

/**
 * Custom exception when a duplicate value is encountered.
 */
public class DuplicateValueException extends RuntimeException{
    public DuplicateValueException(String message) {
        super(message);
    }
}
