package com.harshith.dsa_question_picker.repository;

import com.harshith.dsa_question_picker.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends MongoRepository<User, UUID> {
    Optional<User> findByProviderId(String providerId);
    Optional<User> findByEmail(String email);
}
