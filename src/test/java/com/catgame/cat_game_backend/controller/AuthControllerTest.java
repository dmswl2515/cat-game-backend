package com.catgame.cat_game_backend.controller;

import com.catgame.cat_game_backend.dto.response.TokenResponse;
import com.catgame.cat_game_backend.dto.response.UserResponse;
import com.catgame.cat_game_backend.security.CustomUserDetailsService;
import com.catgame.cat_game_backend.security.JwtTokenProvider;
import com.catgame.cat_game_backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("register - 성공 201")
    @WithMockUser
    void register_성공_201() throws Exception {
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("tester")
                .coins(100)
                .build();

        given(authService.register(any())).willReturn(userResponse);

        Map<String, String> request = Map.of(
                "email", "test@test.com",
                "password", "password123",
                "nickname", "tester"
        );

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andExpect(jsonPath("$.data.nickname").value("tester"));
    }

    @Test
    @DisplayName("register - 유효성 검증 실패 400")
    @WithMockUser
    void register_유효성검증실패_400() throws Exception {
        Map<String, String> request = Map.of(
                "email", "invalid-email",
                "password", "short",
                "nickname", "t"
        );

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("login - 성공 200")
    @WithMockUser
    void login_성공_200() throws Exception {
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();

        given(authService.login(any())).willReturn(tokenResponse);

        Map<String, String> request = Map.of(
                "email", "test@test.com",
                "password", "password123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.data.refreshToken").value("refreshToken"));
    }

    @Test
    @DisplayName("refresh - 성공 200")
    @WithMockUser
    void refresh_성공_200() throws Exception {
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("newAccessToken")
                .refreshToken("newRefreshToken")
                .build();

        given(authService.refresh(any())).willReturn(tokenResponse);

        Map<String, String> request = Map.of(
                "refreshToken", "oldRefreshToken"
        );

        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.data.refreshToken").value("newRefreshToken"));
    }
}
