package com.example.employee_management_api.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a counter document in the MongoDB database.
 * This class is used to manage sequence values for generating unique identifiers
 */
@Data
@Document(collection = "counters")
public class Counter {

    @Id
    private String id;
    private int sequence;
}
