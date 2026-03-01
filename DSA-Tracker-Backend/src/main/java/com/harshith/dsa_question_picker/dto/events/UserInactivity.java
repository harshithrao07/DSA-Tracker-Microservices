package com.harshith.dsa_question_picker.dto.events;

public class UserInactivity {
    private String email;
    private String name;
    private Long duration;

    public UserInactivity(String email, String name, Long duration) {
        this.name = name;
        this.email = email;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}
