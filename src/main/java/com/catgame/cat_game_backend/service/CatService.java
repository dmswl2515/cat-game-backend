package com.catgame.cat_game_backend.service;

import com.catgame.cat_game_backend.domain.entity.Cat;
import com.catgame.cat_game_backend.domain.entity.User;
import com.catgame.cat_game_backend.dto.request.CatCreateRequest;
import com.catgame.cat_game_backend.dto.response.CatResponse;
import com.catgame.cat_game_backend.dto.response.CatStatusResponse;
import com.catgame.cat_game_backend.exception.CustomException;
import com.catgame.cat_game_backend.exception.ErrorCode;
import com.catgame.cat_game_backend.repository.CatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CatService {

    private final CatRepository catRepository;
    private final UserService userService;

    private static final int MAX_CATS_PER_USER = 5;

    @Transactional(readOnly = true)
    public List<CatResponse> getMyCats(Long userId) {
        return catRepository.findAllByOwnerId(userId).stream()
                .map(CatResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CatResponse getCat(Long catId, Long userId) {
        Cat cat = findCatByOwner(catId, userId);
        return CatResponse.from(cat);
    }

    public CatResponse createCat(CatCreateRequest request, Long userId) {
        if (catRepository.countByOwnerId(userId) >= MAX_CATS_PER_USER) {
            throw new CustomException(ErrorCode.CAT_LIMIT_EXCEEDED);
        }

        User owner = userService.findById(userId);
        Cat cat = Cat.builder()
                .name(request.getName())
                .owner(owner)
                .build();

        Cat saved = catRepository.save(cat);
        return CatResponse.from(saved);
    }

    public CatResponse feed(Long catId, Long userId) {
        Cat cat = findCatByOwner(catId, userId);
        cat.feed();
        return CatResponse.from(cat);
    }

    public CatResponse water(Long catId, Long userId) {
        Cat cat = findCatByOwner(catId, userId);
        cat.water();
        return CatResponse.from(cat);
    }

    public CatResponse clean(Long catId, Long userId) {
        Cat cat = findCatByOwner(catId, userId);
        cat.clean();
        return CatResponse.from(cat);
    }

    @Transactional(readOnly = true)
    public CatStatusResponse getStatus(Long catId, Long userId) {
        Cat cat = findCatByOwner(catId, userId);
        return CatStatusResponse.from(cat);
    }

    private Cat findCatByOwner(Long catId, Long userId) {
        return catRepository.findByIdAndOwnerId(catId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAT_NOT_FOUND));
    }
}
