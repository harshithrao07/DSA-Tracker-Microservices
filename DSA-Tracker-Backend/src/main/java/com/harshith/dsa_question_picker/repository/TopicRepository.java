package com.harshith.dsa_question_picker.repository;

import com.harshith.dsa_question_picker.model.Topic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TopicRepository extends MongoRepository<Topic, UUID> {
    List<Topic> findByCreatedBy(UUID createdBy);

    boolean existsByNameAndCreatedBy(String name, UUID createdBy);

    Optional<Topic> findByNameAndCreatedBy(String name, UUID createdBy);

    List<Topic> findByNameInAndCreatedBy(List<String> names, UUID createdBy);
}
