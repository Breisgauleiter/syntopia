package com.syntopia.controller;

import com.syntopia.service.QuestService;
import com.syntopia.service.UserService;
import com.syntopia.config.JwtTokenUtil;
import com.syntopia.config.JwtAuthenticationEntryPoint;
import com.syntopia.config.JwtRequestFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserQuestController.class)
@AutoConfigureMockMvc(addFilters = true)
@WithMockUser(username = "user-1", roles = {"USER"})
class UserQuestControllerErrorHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestService questService;

        // Mock security-related dependencies to satisfy JwtRequestFilter & SecurityConfig wiring
        @MockBean
        private UserService userService;
        @MockBean
        private JwtTokenUtil jwtTokenUtil;
        @MockBean
        private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

                @MockBean
                private JwtRequestFilter jwtRequestFilter;


    @Test
    @DisplayName("acceptQuest returns ApiResponse error envelope on IllegalArgumentException")
    void acceptQuest_illegalArgument_mapsTo400Envelope() throws Exception {
                // Bypass JWT filter behavior
                org.mockito.Mockito.doAnswer(invocation -> {
                        jakarta.servlet.FilterChain chain = invocation.getArgument(2);
                        jakarta.servlet.http.HttpServletRequest req = invocation.getArgument(0);
                        jakarta.servlet.http.HttpServletResponse res = invocation.getArgument(1);
                        chain.doFilter(req, res);
                        return null;
                }).when(jwtRequestFilter).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        when(userService.loadUserByUsername(anyString())).thenReturn(org.springframework.security.core.userdetails.User.withUsername("user-1").password("").authorities("ROLE_USER").build());
        when(questService.acceptUserQuest("user-1", "quest-123"))
                .thenThrow(new IllegalArgumentException("Quest not found"));

        mockMvc.perform(post("/api/user-quests/quest-123/accept").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Quest not found"));
    }

    @Test
    @DisplayName("completeQuest returns generic internal error envelope on unexpected Exception")
    void completeQuest_genericException_mapsTo500Envelope() throws Exception {
                org.mockito.Mockito.doAnswer(invocation -> {
                        jakarta.servlet.FilterChain chain = invocation.getArgument(2);
                        jakarta.servlet.http.HttpServletRequest req = invocation.getArgument(0);
                        jakarta.servlet.http.HttpServletResponse res = invocation.getArgument(1);
                        chain.doFilter(req, res);
                        return null;
                }).when(jwtRequestFilter).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        when(userService.loadUserByUsername(anyString())).thenReturn(org.springframework.security.core.userdetails.User.withUsername("user-1").password("").authorities("ROLE_USER").build());
        when(questService.completeUserQuest(eq("user-1"), eq("quest-500"), anyMap()))
                .thenThrow(new RuntimeException("DB down"));

        mockMvc.perform(post("/api/user-quests/quest-500/complete").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dummy\":true}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Internal server error"))
                .andExpect(jsonPath("$.details").value("DB down"));
    }

    @Test
    @DisplayName("updateQuestProgress returns 400 envelope on IllegalArgumentException bubbled")
    void updateQuestProgress_illegalArgument() throws Exception {
                org.mockito.Mockito.doAnswer(invocation -> {
                        jakarta.servlet.FilterChain chain = invocation.getArgument(2);
                        jakarta.servlet.http.HttpServletRequest req = invocation.getArgument(0);
                        jakarta.servlet.http.HttpServletResponse res = invocation.getArgument(1);
                        chain.doFilter(req, res);
                        return null;
                }).when(jwtRequestFilter).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        when(userService.loadUserByUsername(anyString())).thenReturn(org.springframework.security.core.userdetails.User.withUsername("user-1").password("").authorities("ROLE_USER").build());
        when(questService.updateQuestProgress(eq("user-1"), eq("quest-xyz"), any()))
                .thenThrow(new IllegalArgumentException("Invalid progress data"));

        mockMvc.perform(put("/api/user-quests/quest-xyz/progress").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"progress\":-10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid progress data"));
    }
}
