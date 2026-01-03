package com.teraapi.integration;

import com.teraapi.identity.controller.AuthController;
import com.teraapi.identity.dto.TokenValidationResponse;
import com.teraapi.identity.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class IdentityStreamContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    void introspectionEndpointReturnsActivePayload() throws Exception {
        TokenValidationResponse response = TokenValidationResponse.builder()
                .active(true)
                .username("stream-service")
                .role("SERVICE")
                .deviceId("stream-node-01")
                .issuedAt(1704198000000L)
                .expiresAt(1704201600000L)
                .build();

        Mockito.when(authenticationService.validateToken(eq("valid-token")))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/introspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"valid-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.username").value("stream-service"))
                .andExpect(jsonPath("$.role").value("SERVICE"))
                .andExpect(jsonPath("$.deviceId").value("stream-node-01"))
                .andExpect(jsonPath("$.issuedAt").value(1704198000000L))
                .andExpect(jsonPath("$.expiresAt").value(1704201600000L));
    }

    @Test
    void introspectionEndpointFlagsInactiveToken() throws Exception {
        Mockito.when(authenticationService.validateToken(eq("expired-token")))
                .thenReturn(TokenValidationResponse.inactive());

        mockMvc.perform(post("/api/v1/auth/introspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"expired-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.active").value(false));
    }
}
