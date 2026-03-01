package com.harshith.dsa_question_picker.config;

import com.harshith.dsa_question_picker.dto.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public NewTopic userInactivityTopic() {
        return TopicBuilder.name(Topics.USER_INACTIVE)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
