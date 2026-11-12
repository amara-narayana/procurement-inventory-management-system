package com.zycus.pims.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zycus.pims.auth.dto.LoginRequest;
import com.zycus.pims.auth.dto.RegisterRequest;
import com.zycus.pims.common.enums.RoleName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterAndLoginIntegration() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "sarah.connor",
                "secretPassword123",
                "sarah.c@zycus.com",
                "Sarah",
                "Connor",
                "Operations",
                Set.of(RoleName.EMPLOYEE)
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("sarah.connor"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());

        LoginRequest loginReq = new LoginRequest("sarah.connor", "secretPassword123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void testLoginWithInvalidCredentialsReturnsUnauthorized() throws Exception {
        LoginRequest badLogin = new LoginRequest("nonexistent.user", "wrongpwd");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
