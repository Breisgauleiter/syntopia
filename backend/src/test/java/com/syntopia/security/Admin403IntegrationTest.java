package com.syntopia.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import com.syntopia.model.User;
import com.syntopia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifies non-admin authenticated user gets 403 with ApiResponse envelope on admin endpoint. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class Admin403IntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestJwtUtil testJwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.secret}")
    private String secretLoaded; // forces property load for context sanity

    @Test
    void nonAdminUser_receives403_onAdminQuestSeed() {
        // Ensure user exists without admin role
        String username = "regular-user";
        userRepository.findByUsername(username).orElseGet(() -> {
            User u = new User(username, username + "@example.test");
            u.setDisplayName("Regular User");
            return userRepository.save(u);
        });
        String token = testJwtUtil.userToken(username);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port + "/api/quests/seed-onboarding", HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).contains("application/json");
        assertThat(response.getBody()).contains("\"success\":false").contains("Access denied");
    }
}
