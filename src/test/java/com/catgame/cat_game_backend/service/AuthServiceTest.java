package com.catgame.cat_game_backend.service;

import com.catgame.cat_game_backend.domain.entity.User;
import com.catgame.cat_game_backend.dto.request.LoginRequest;
import com.catgame.cat_game_backend.dto.request.SignUpRequest;
import com.catgame.cat_game_backend.dto.request.TokenRefreshRequest;
import com.catgame.cat_game_backend.dto.response.TokenResponse;
import com.catgame.cat_game_backend.dto.response.UserResponse;
import com.catgame.cat_game_backend.exception.CustomException;
import com.catgame.cat_game_backend.exception.ErrorCode;
import com.catgame.cat_game_backend.repository.UserRepository;
import com.catgame.cat_game_backend.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private User createUser() {
        User user = User.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .nickname("tester")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private SignUpRequest createSignUpRequest() {
        SignUpRequest request = new SignUpRequest();
        ReflectionTestUtils.setField(request, "email", "test@test.com");
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "nickname", "tester");
        return request;
    }

    private LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "email", "test@test.com");
        ReflectionTestUtils.setField(request, "password", "password123");
        return request;
    }

    private TokenRefreshRequest createTokenRefreshRequest(String token) {
        TokenRefreshRequest request = new TokenRefreshRequest();
        ReflectionTestUtils.setField(request, "refreshToken", token);
        return request;
    }

    @Test
    @DisplayName("register - 성공")
    void register_성공() {
        SignUpRequest request = createSignUpRequest();
        User user = createUser();

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(userRepository.existsByNickname(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(user);

        UserResponse response = authService.register(request);

        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getNickname()).isEqualTo("tester");
    }

    @Test
    @DisplayName("register - 이메일 중복 실패")
    void register_이메일중복_실패() {
        SignUpRequest request = createSignUpRequest();

        given(userRepository.existsByEmail(anyString())).willReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATE_EMAIL));
    }

    @Test
    @DisplayName("register - 닉네임 중복 실패")
    void register_닉네임중복_실패() {
        SignUpRequest request = createSignUpRequest();

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(userRepository.existsByNickname(anyString())).willReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATE_NICKNAME));
    }

    @Test
    @DisplayName("login - 성공")
    void login_성공() {
        LoginRequest request = createLoginRequest();
        User user = createUser();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtTokenProvider.createAccessToken(anyLong())).willReturn("accessToken");
        given(jwtTokenProvider.createRefreshToken(anyLong())).willReturn("refreshToken");

        TokenResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
    }

    @Test
    @DisplayName("login - 비밀번호 불일치 실패")
    void login_비밀번호불일치_실패() {
        LoginRequest request = createLoginRequest();
        User user = createUser();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_PASSWORD));
    }

    @Test
    @DisplayName("login - 유저 없음 실패")
    void login_유저없음_실패() {
        LoginRequest request = createLoginRequest();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("refresh - 성공")
    void refresh_성공() {
        String oldRefreshToken = "oldRefreshToken";
        TokenRefreshRequest request = createTokenRefreshRequest(oldRefreshToken);
        User user = createUser();
        user.updateRefreshToken(oldRefreshToken);

        given(jwtTokenProvider.validateToken(oldRefreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserIdFromToken(oldRefreshToken)).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(jwtTokenProvider.createAccessToken(1L)).willReturn("newAccessToken");
        given(jwtTokenProvider.createRefreshToken(1L)).willReturn("newRefreshToken");

        TokenResponse response = authService.refresh(request);

        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");
    }

    @Test
    @DisplayName("refresh - 토큰 불일치 실패")
    void refresh_토큰불일치_실패() {
        String requestToken = "requestToken";
        TokenRefreshRequest request = createTokenRefreshRequest(requestToken);
        User user = createUser();
        user.updateRefreshToken("differentToken");

        given(jwtTokenProvider.validateToken(requestToken)).willReturn(true);
        given(jwtTokenProvider.getUserIdFromToken(requestToken)).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.REFRESH_TOKEN_MISMATCH));
    }
}
