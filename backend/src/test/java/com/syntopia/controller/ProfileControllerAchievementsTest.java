package com.syntopia.controller;

import com.syntopia.service.ProfileService;
import com.syntopia.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProfileController.class)
@AutoConfigureMockMvc(addFilters = true)
class ProfileControllerAchievementsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private ProfileService profileService;
    @MockBean private UserService userService;
    @MockBean private com.syntopia.config.JwtRequestFilter jwtRequestFilter;
    @MockBean private com.syntopia.config.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @MockBean private com.syntopia.config.JwtTokenUtil jwtTokenUtil;

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void achievements_returnsEnvelopeAndList() throws Exception {
        // Bypass JWT filter to allow request to proceed
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtRequestFilter).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        // mock user lookup
        var mockUser = new com.syntopia.model.User();
        mockUser.setId("u1");
        mockUser.setUsername("user1");
        when(userService.findByUsername("user1")).thenReturn(java.util.Optional.of(mockUser));

        Map<String,Object> ach = new HashMap<>();
        ach.put("id", "first_project");
        ach.put("title", "First Project");
        ach.put("status", "completed");
        Map<String,Object> payload = new HashMap<>();
        payload.put("achievements", List.of(ach));
        payload.put("totalAchievements", 1);
        payload.put("completedAchievements", 1);
        when(profileService.getUserAchievements("u1")).thenReturn(payload);

        mockMvc.perform(get("/api/profile/achievements").with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.achievements[0].id").value("first_project"))
            .andExpect(jsonPath("$.data.totalAchievements").value(1));
    }
}
