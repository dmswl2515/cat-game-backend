package com.catgame.cat_game_backend.service;

import com.catgame.cat_game_backend.domain.entity.User;
import com.catgame.cat_game_backend.dto.response.UserResponse;
import com.catgame.cat_game_backend.exception.CustomException;
import com.catgame.cat_game_backend.exception.ErrorCode;
import com.catgame.cat_game_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public UserResponse getUserInfo(Long userId) {
        User user = findById(userId);
        return UserResponse.from(user);
    }
}
