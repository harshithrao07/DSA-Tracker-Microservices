package com.harshith.dsa_question_picker.schedulers;

import com.harshith.dsa_question_picker.dto.Topics;
import com.harshith.dsa_question_picker.dto.events.UserInactivity;
import com.harshith.dsa_question_picker.model.Topic;
import com.harshith.dsa_question_picker.model.User;
import com.harshith.dsa_question_picker.repository.UserRepository;
import com.harshith.dsa_question_picker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InactivityScheduler {

    private final UserRepository userRepository;
    private final UserService userService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(cron = "0 0 9 * * ?") // every day 9 AM
    public void checkInactiveUsers() {

        List<User> users = userRepository.findAll();

        for (User user : users) {
            boolean inactive = userService.isUserInactive(user.getId());
            if (inactive) {
                UserInactivity event = new UserInactivity(
                        user.getName(),
                        user.getEmail(),
                        7L
                );

                kafkaTemplate.send(Topics.USER_INACTIVE, event);
            }
        }
    }
}