package com.syntopia.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.syntopia.model.User;
import com.syntopia.config.JwtTokenUtil;
import com.syntopia.config.JwtRequestFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.syntopia.service.UserService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = SimpleAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class SimpleAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    @Test
    void register_returns200() throws Exception {
        // Minimal happy path just asserts structure; service mocked
    when(userService.findByEmail(anyString())).thenReturn(Optional.empty());
    when(userService.findByUsername(anyString())).thenReturn(Optional.empty());
    when(userService.createUser(anyString(), anyString(), anyString()))
        .thenReturn(new User());
    // return hashed pwd and echo saved user
    when(passwordEncoder.encode(anyString())).thenReturn("hashed");
    when(userService.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String body = "{\n" +
                "  \"username\": \"testuser\",\n" +
                "  \"email\": \"test@syntopia.local\",\n" +
                "  \"password\": \"secret\",\n" +
                "  \"displayName\": \"Test User\"\n" +
                "}";

    mockMvc.perform(post("/api/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.user").exists());
    }

    @Test
    void me_unauthorized_without_token() throws Exception {
    // Provide an invalid/missing-style header to hit controller's 401 branch
    mockMvc.perform(get("/api/auth/me").header("Authorization", ""))
            .andExpect(status().isUnauthorized());
    }
}
