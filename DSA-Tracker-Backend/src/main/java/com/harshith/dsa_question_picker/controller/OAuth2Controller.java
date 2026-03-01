package com.harshith.dsa_question_picker.controller;

import com.harshith.dsa_question_picker.model.OAuth2Provider;
import com.harshith.dsa_question_picker.security.OAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class OAuth2Controller {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String githubClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String githubClientSecret;

    @Value("${spring.security.oauth2.client.registration.github.redirect-uri}")
    private String githubRedirectUri;

    private final OAuth2Service oAuth2Service;

    @GetMapping("/google/callback")
    public ResponseEntity<?> handleGoogleCallback(@RequestParam String code) {
        try {
            String tokenEndpoint = "https://oauth2.googleapis.com/token";
            String idToken = oAuth2Service.getAccessToken(tokenEndpoint, code, googleClientId, googleClientSecret, googleRedirectUri);

            String userInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            return oAuth2Service.loadUser(userInfoUrl, OAuth2Provider.google);
        } catch (Exception e) {
            log.error("Exception occurred while handleGoogleCallback ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/github/callback")
    public ResponseEntity<?> handleGithubCallback(@RequestParam String code) {
        try {
            String tokenEndpoint = "https://github.com/login/oauth/access_token";
            String accessToken = oAuth2Service.getAccessToken(tokenEndpoint, code, githubClientId, githubClientSecret, githubRedirectUri);

            String userInfoUrl = "https://api.github.com/user";
            return oAuth2Service.loadUserWithToken(userInfoUrl, OAuth2Provider.github, accessToken);
        } catch (Exception e) {
            log.error("Exception occurred while handleGithubCallback ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
