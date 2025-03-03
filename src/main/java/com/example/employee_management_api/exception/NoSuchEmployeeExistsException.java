package com.example.employee_management_api.exception;

public class NoSuchEmployeeExistsException extends RuntimeException {
    public NoSuchEmployeeExistsException(String message) {
        super(message);
    }
}
