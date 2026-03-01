package com.harshith.notification_service.service;

import com.harshith.notification_service.dto.Topics;
import com.harshith.notification_service.dto.events.UserInactivity;
import jakarta.mail.internet.MimeMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotificationService {

    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @KafkaListener(topics = Topics.USER_INACTIVE, groupId = "dsa-group")
    public void notifyUserOfInactivity(UserInactivity event) {
        String name = event.getName();
        String email = event.getEmail();
        Long duration = event.getDuration();

        sendMail(email, name, duration);
    }

    @Async
    public void sendMail(String email, String name, Long duration) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("We Miss You at DSA Tracker 🚀");

            String htmlContent = buildTemplate(name, duration);

            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String buildTemplate(String name, Long duration) {

        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color:#f4f6f8; padding:20px;">
                
                    <div style="max-width:600px; margin:auto; background:white; padding:30px; border-radius:10px;">
                        
                        <h2 style="color:#2c3e50;">Hey %s 👋</h2>
                        
                        <p style="font-size:16px; color:#555;">
                            We noticed that you haven't practiced DSA for <b>%d days</b>.
                        </p>
                        
                        <p style="font-size:16px; color:#555;">
                            Consistency is the key to mastering problem solving.
                            Jump back in and continue your streak 🔥
                        </p>
                        
                        <div style="text-align:center; margin:30px 0;">
                            <a href="http://localhost:3000"
                               style="background-color:#4CAF50; color:white; padding:12px 25px;
                                      text-decoration:none; border-radius:5px;">
                                Resume Practice
                            </a>
                        </div>
                        
                        <p style="font-size:14px; color:#999;">
                            Keep coding,<br>
                            Team DSA Tracker ❤️
                        </p>
                    
                    </div>
                
                </body>
                </html>
                """.formatted(name, duration);
    }
}