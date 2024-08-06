package dev.sosnovsky.task.management.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sosnovsky.task.management.system.dto.LoginRequest;
import dev.sosnovsky.task.management.system.dto.TokensResponse;
import dev.sosnovsky.task.management.system.exception.LoginOrPasswordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Успешное создание токенов")
    @DirtiesContext
    void createAuthToken() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/auth/login")
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("asanta@user.com", "password1")))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        TokensResponse response = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(), TokensResponse.class);
        assertFalse(response.accessToken().isBlank());
        assertFalse(response.refreshToken().isBlank());
    }

    @Test
    @DisplayName("Запрос токенов с неверным паролем")
    @DirtiesContext
    void requestTokensWithInvalidPassword() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("asanta@user.com", "password55")))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(result -> assertInstanceOf(LoginOrPasswordException.class,
                        result.getResolvedException()));
    }
}