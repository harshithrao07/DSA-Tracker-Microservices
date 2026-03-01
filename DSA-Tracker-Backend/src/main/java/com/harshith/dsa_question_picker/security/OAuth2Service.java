package com.harshith.dsa_question_picker.security;

import com.harshith.dsa_question_picker.model.OAuth2Provider;
import com.harshith.dsa_question_picker.model.User;
import com.harshith.dsa_question_picker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuth2Service {
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    @Value("${frontend.url}")
    private String frontendUrl;

    public ResponseEntity<?> loadUser(String userInfoUrl, OAuth2Provider provider) {
        ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);
        if (userInfoResponse.getStatusCode() == HttpStatus.OK) {
            return handleUserInfo(userInfoResponse.getBody(), provider);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    public ResponseEntity<?> loadUserWithToken(String userInfoUrl, OAuth2Provider provider, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> userInfoResponse =
                restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, Map.class);

        if (userInfoResponse.getStatusCode() == HttpStatus.OK) {
            return handleUserInfo(userInfoResponse.getBody(), provider);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    private ResponseEntity<?> handleUserInfo(Map<String, Object> userInfo, OAuth2Provider provider) {
        String email = (String) userInfo.getOrDefault("email", "");
        String name = (String) userInfo.getOrDefault("name", null);
        String picture;
        String providerId;

        if (provider.equals(OAuth2Provider.google)) {
            providerId = (String) userInfo.get("sub");
            picture = (String) userInfo.getOrDefault("picture", "");
        } else if (provider.equals(OAuth2Provider.github)) {
            providerId = String.valueOf(userInfo.get("id"));
            if (name == null) {
                name = (String) userInfo.getOrDefault("login", null);
            }
            picture = (String) userInfo.getOrDefault("avatar_url", "");
        } else {
            providerId = null;
            picture = null;
        }

        String finalName = name != null ? name : email;

        User user = userRepository
                .findByProviderId(providerId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .id(UUID.randomUUID())
                            .email(email)
                            .name(finalName)
                            .pictureUrl(picture)
                            .provider(provider.toString())
                            .providerId(providerId)
                            .build();
                    return userRepository.save(newUser);
                });

        user.setEmail(email);
        user.setName(finalName);
        user.setPictureUrl(picture);
        user = userRepository.save(user);

        // Set in Spring Security context
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String subject = providerId;
        Map<String, Object> claims = new HashMap<>();
        if (email != null) {
            claims.put("email", email);
        }
        claims.put("provider", provider.toString());

        String jwtToken = jwtService.createToken(claims, subject);

        String redirectUrl = frontendUrl + "/dashboard?token=" + jwtToken;

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();

    }

    public String getAccessToken(String tokenEndpoint, String code,
                                 String clientId, String clientSecret, String redirectUri) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);

        // For Google, must include this
        if (tokenEndpoint.contains("googleapis")) {
            params.add("grant_type", "authorization_code");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint, request, Map.class);
        Map<String, Object> body = tokenResponse.getBody();

        if (body == null) {
            throw new RuntimeException("Empty token response from " + tokenEndpoint);
        }

        if (tokenEndpoint.contains("googleapis")) {
            return (String) body.get("id_token"); // Google
        } else {
            return (String) body.get("access_token"); // GitHub
        }
    }
}