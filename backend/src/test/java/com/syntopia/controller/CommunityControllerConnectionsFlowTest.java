package com.syntopia.controller;

import com.syntopia.model.User;
import com.syntopia.service.CommunityService;
import com.syntopia.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Connection lifecycle happy-path controller contract tests.
 * Ensures envelope structure & key fields for connect/accept/decline/cancel endpoints.
 */
@WebMvcTest(controllers = CommunityController.class)
@AutoConfigureMockMvc(addFilters = true)
class CommunityControllerConnectionsFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private CommunityService communityService;
    @MockBean private UserService userService;
    @MockBean private com.syntopia.config.JwtRequestFilter jwtRequestFilter;
    @MockBean private com.syntopia.config.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @MockBean private com.syntopia.config.JwtTokenUtil jwtTokenUtil;

    private void bypassJwt() throws Exception {
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtRequestFilter).doFilter(any(), any(), any());
    }

    private User mockUser(String id, String username) {
        User u = new User();
        u.setId(id); u.setUsername(username); return u;
    }

    @Test
    @DisplayName("POST connect -> PUT accept -> PUT decline -> DELETE cancel returns envelopes")
    @WithMockUser(username = "alice", roles = {"USER"})
    void connectionLifecycle_envelopes() throws Exception {
        bypassJwt();
        when(userService.findByUsername("alice")).thenReturn(Optional.of(mockUser("u-alice", "alice")));

        // Send request
        Map<String,Object> sendPayload = new HashMap<>();
        sendPayload.put("id", "conn-1");
        sendPayload.put("status", "PENDING");
        sendPayload.put("fromUserId", "u-alice");
        sendPayload.put("toUserId", "u-bob");
    when(communityService.sendConnectionRequest(eq("u-alice"), eq("u-bob"), eq("friend")))
                .thenReturn(sendPayload);

    mockMvc.perform(post("/api/community/connect").with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\n  \"toUserId\": \"u-bob\", \n  \"connectionType\": \"friend\"\n}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value("conn-1"))
            .andExpect(jsonPath("$.data.fromUserId").value("u-alice"))
            .andExpect(jsonPath("$.data.toUserId").value("u-bob"))
            .andExpect(jsonPath("$.data.status").value("PENDING"));

        // Accept request
        Map<String,Object> acceptPayload = new HashMap<>(sendPayload);
        acceptPayload.put("status", "ACCEPTED");
        when(userService.findByUsername("alice")).thenReturn(Optional.of(mockUser("u-alice", "alice"))); // re-stub
        when(communityService.respondToConnectionRequest("conn-1", "accept", "u-alice")).thenReturn(acceptPayload);

        mockMvc.perform(put("/api/community/connect/conn-1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n  \"action\": \"accept\"\n}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("ACCEPTED"));

        // Decline scenario (second connection)
        Map<String,Object> declinePayload = new HashMap<>(sendPayload);
        declinePayload.put("id", "conn-2");
        declinePayload.put("status", "DECLINED");
        when(userService.findByUsername("alice")).thenReturn(Optional.of(mockUser("u-alice", "alice")));
        when(communityService.respondToConnectionRequest("conn-2", "decline", "u-alice"))
                .thenReturn(declinePayload);

        mockMvc.perform(put("/api/community/connect/conn-2").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n  \"action\": \"decline\"\n}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("DECLINED"));

        // Cancel outgoing pending connection
        Map<String,Object> cancelPayload = new HashMap<>();
        cancelPayload.put("cancelled", true);
        cancelPayload.put("id", "conn-3");
        when(userService.findByUsername("alice")).thenReturn(Optional.of(mockUser("u-alice", "alice")));
        when(communityService.cancelPendingConnection("conn-3", "u-alice")).thenReturn(cancelPayload);

        mockMvc.perform(delete("/api/community/connect/conn-3").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.cancelled").value(true))
            .andExpect(jsonPath("$.data.id").value("conn-3"));
    }
}
