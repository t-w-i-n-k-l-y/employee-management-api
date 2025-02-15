package com.example.employee_management_api.exception;

public class DuplicateValueException extends RuntimeException{
    public DuplicateValueException(String message) {
        super(message);
    }
}
