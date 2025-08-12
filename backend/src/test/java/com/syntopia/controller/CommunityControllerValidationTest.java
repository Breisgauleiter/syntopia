package com.syntopia.controller;

import com.syntopia.config.JwtAuthenticationEntryPoint;
import com.syntopia.config.JwtRequestFilter;
import com.syntopia.config.JwtTokenUtil;
import com.syntopia.service.CommunityService;
import com.syntopia.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommunityController.class)
@AutoConfigureMockMvc(addFilters = true)
class CommunityControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private CommunityService communityService;
    @MockBean private UserService userService;
    @MockBean private JwtRequestFilter jwtRequestFilter;
    @MockBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @MockBean private JwtTokenUtil jwtTokenUtil;

    @Test
    @WithMockUser(username = "user-1", roles = {"USER"})
    void sendConnectionRequest_validationErrors_mapTo400Envelope() throws Exception {
        // Bypass JWT filter to avoid requiring actual token
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            jakarta.servlet.http.HttpServletRequest req = invocation.getArgument(0);
            jakarta.servlet.http.HttpServletResponse res = invocation.getArgument(1);
            chain.doFilter(req, res);
            return null;
        }).when(jwtRequestFilter).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        // Missing required fields
        String body = "{}";

        mockMvc.perform(post("/api/community/connect").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.validationErrors.toUserId").exists())
                .andExpect(jsonPath("$.details.validationErrors.connectionType").exists());
    }
}
