package com.syntopia.controller;

import com.syntopia.service.ProfileService;
import com.syntopia.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
@Disabled("Obsolete after relying solely on SecurityFilterChain for 401 handling")
class ProfileControllerUnauthorizedTest {

    @Autowired
    private MockMvc mockMvc;

    // Beans required by the controller, though not used in this test path
    @MockBean private ProfileService profileService;
    @MockBean private UserService userService;
    // Mock security-layer beans to satisfy context
    @MockBean private com.syntopia.config.JwtRequestFilter jwtRequestFilter;
    @MockBean private com.syntopia.config.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @MockBean private com.syntopia.config.JwtTokenUtil jwtTokenUtil;

    @Test
    @DisplayName("GET /api/profile without Authentication -> 401 envelope from GlobalExceptionHandler")
    void getProfile_withoutAuth_returns401Envelope() throws Exception {
        mockMvc.perform(get("/api/profile")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Authentication required"));
    }
}
