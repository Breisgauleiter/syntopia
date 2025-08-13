package com.syntopia.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import com.syntopia.service.ProfileService;
import com.syntopia.service.UserService;
import com.syntopia.config.JwtRequestFilter;
import com.syntopia.config.JwtAuthenticationEntryPoint;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Verifies that the Spring Security authentication entry point returns
 * a standardized ApiResponse envelope for unauthenticated access to a secured endpoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Disabled("Replaced by secure-test entry point integration test")
class SecurityEntryPointUnauthorizedTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private ProfileService profileService;
    @MockBean private UserService userService;
    // Mock the JWT filter so it doesn't authenticate; real entry point bean is used
    @MockBean private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint; // ensure bean loads

    @Test
    void unauthenticated_access_returns_json_401_envelope() throws Exception {
    mockMvc.perform(get("/api/profile").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }
}
