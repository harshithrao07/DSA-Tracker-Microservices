package com.harshith.dsa_question_picker.config;

import com.mongodb.client.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    private final MongoClient mongoClient;

    public MongoConfig(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        // Database name should match the one in your connection URI
        return new MongoTemplate(mongoClient, "dsa_tracker");
    }
}
