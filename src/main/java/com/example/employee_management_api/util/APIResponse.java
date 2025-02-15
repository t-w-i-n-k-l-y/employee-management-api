package com.example.employee_management_api.util;

import lombok.Data;

/**
 * Generic API response wrapper class.
 * It supports generic types to accommodate different response payloads.
 * @param <T> The type of data included in the response.
 */
@Data
public class APIResponse<T> {
    private String message;
    private T data;
    private int statusCode;

    public APIResponse(String message, T data, int statusCode) {
        this.message = message;
        this.data = data;
        this.statusCode = statusCode;
    }
}
