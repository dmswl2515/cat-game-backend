package com.catgame.cat_game_backend.controller;

import com.catgame.cat_game_backend.domain.enums.CatAge;
import com.catgame.cat_game_backend.domain.enums.HealthStatus;
import com.catgame.cat_game_backend.dto.response.CatResponse;
import com.catgame.cat_game_backend.dto.response.CatStatusResponse;
import com.catgame.cat_game_backend.security.CustomUserDetailsService;
import com.catgame.cat_game_backend.security.JwtTokenProvider;
import com.catgame.cat_game_backend.service.CatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatController.class)
class CatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CatService catService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private CatResponse createCatResponse() {
        return CatResponse.builder()
                .id(1L)
                .name("나비")
                .hunger(80)
                .thirst(80)
                .cleanliness(80)
                .happiness(80)
                .age(CatAge.KITTEN)
                .health(HealthStatus.HEALTHY)
                .lastFed(LocalDateTime.now())
                .lastWatered(LocalDateTime.now())
                .lastCleaned(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getMyCats - 성공 200")
    void getMyCats_성공_200() throws Exception {
        CatResponse catResponse = createCatResponse();
        given(catService.getMyCats(1L)).willReturn(List.of(catResponse));

        mockMvc.perform(get("/api/cats")
                        .with(user("1").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("나비"));
    }

    @Test
    @DisplayName("createCat - 성공 201")
    void createCat_성공_201() throws Exception {
        CatResponse catResponse = createCatResponse();
        given(catService.createCat(any(), anyLong())).willReturn(catResponse);

        Map<String, String> request = Map.of("name", "나비");

        mockMvc.perform(post("/api/cats")
                        .with(user("1").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("나비"));
    }

    @Test
    @DisplayName("feed - 성공 200")
    void feed_성공_200() throws Exception {
        CatResponse catResponse = CatResponse.builder()
                .id(1L)
                .name("나비")
                .hunger(100)
                .thirst(80)
                .cleanliness(80)
                .happiness(85)
                .age(CatAge.KITTEN)
                .health(HealthStatus.HEALTHY)
                .lastFed(LocalDateTime.now())
                .lastWatered(LocalDateTime.now())
                .lastCleaned(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        given(catService.feed(1L, 1L)).willReturn(catResponse);

        mockMvc.perform(post("/api/cats/1/feed")
                        .with(user("1").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hunger").value(100));
    }

    @Test
    @DisplayName("getStatus - 성공 200")
    void getStatus_성공_200() throws Exception {
        CatStatusResponse statusResponse = CatStatusResponse.builder()
                .id(1L)
                .name("나비")
                .hunger(80)
                .thirst(80)
                .cleanliness(80)
                .happiness(80)
                .health(HealthStatus.HEALTHY)
                .needsFeeding(false)
                .needsWater(false)
                .needsCleaning(false)
                .alert(false)
                .build();

        given(catService.getStatus(1L, 1L)).willReturn(statusResponse);

        mockMvc.perform(get("/api/cats/1/status")
                        .with(user("1").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.needsFeeding").value(false))
                .andExpect(jsonPath("$.data.alert").value(false));
    }

    @Test
    @DisplayName("인증 없이 요청 - 401")
    void 인증없이_요청_401() throws Exception {
        mockMvc.perform(get("/api/cats"))
                .andExpect(status().isUnauthorized());
    }
}
