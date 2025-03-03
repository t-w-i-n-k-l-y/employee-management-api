package com.example.employee_management_api.service;

import com.example.employee_management_api.exception.ResourceNotFoundException;
import com.example.employee_management_api.model.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing sequential counters in MongoDB.
 */
@Service
public class CounterService {

    private static final Logger logger = LoggerFactory.getLogger(CounterService.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Retrieves and increments the sequence number for a given counter.
     *
     * @param counterName The name of the counter to increment.
     * @return The next sequence number for the given counter.
     */
    @Transactional
    public int getNextSequence(String counterName) {

        try {
            logger.info("Fetching next sequence for counter: {}", counterName);

            // Define the query to find the counter document by its ID
            Query query = Query.query(Criteria.where("id").is(counterName));

            Update update = new Update().inc("sequence", 1);
            FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true).upsert(true);
            Counter counter = mongoTemplate.findAndModify(query, update, options, Counter.class);

            if (counter == null) {
                logger.warn("Counter document for {} was not found; returning default sequence value 1.", counterName);
                throw new ResourceNotFoundException("No counter found or unable to get the next available employee ID");
            }

            logger.info("New sequence value for {}: {}", counterName, counter.getSequence());
            return counter.getSequence();

        } catch (DataAccessException e) {
            logger.error("Database error while fetching sequence number for {}: {}", counterName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve next sequence for counter: " + counterName, e);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching sequence for {}: {}", counterName, e.getMessage(), e);
            throw new RuntimeException("Unexpected error while fetching sequence for counter: " + counterName, e);
        }
    }
}
