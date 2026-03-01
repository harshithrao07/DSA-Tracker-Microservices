package com.harshith.dsa_question_picker.service;

import com.harshith.dsa_question_picker.dto.user.LastActivityDTO;
import com.harshith.dsa_question_picker.repository.NoteRepository;
import com.harshith.dsa_question_picker.repository.QuestionRepository;
import com.harshith.dsa_question_picker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final NoteRepository noteRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByProviderId(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public boolean isUserInactive(UUID userId) {

        LocalDateTime questionDate =
                questionRepository.getLastQuestionActivity(userId)
                        .map(LastActivityDTO::getLastActivity)
                        .orElse(null);

        LocalDateTime noteDate =
                noteRepository.getLastNoteActivity(userId)
                        .map(LastActivityDTO::getLastActivity)
                        .orElse(null);

        LocalDateTime lastActivity = Stream.of(questionDate, noteDate)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        if (lastActivity == null) {
            return true; // never used app
        }

        return lastActivity.isBefore(LocalDateTime.now().minusDays(7));
    }
}
