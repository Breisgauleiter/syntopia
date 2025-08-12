package com.syntopia.security;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class UnauthorizedEndpointsParameterizedTest {

    @Autowired
    private MockMvc mockMvc;

    static Stream<String> unauthorizedGetEndpoints() {
        return Stream.of(
            "/api/profile",
            "/api/profile/achievements",
            "/api/profile/social"
        );
    }

    @ParameterizedTest
    @MethodSource("unauthorizedGetEndpoints")
    void endpoint_requires_auth_returns_standard_401(String path) throws Exception {
        mockMvc.perform(get(path).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Authentication required"));
    }
}
