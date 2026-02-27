package com.catgame.cat_game_backend.service;

import com.catgame.cat_game_backend.domain.entity.Cat;
import com.catgame.cat_game_backend.domain.entity.User;
import com.catgame.cat_game_backend.dto.request.CatCreateRequest;
import com.catgame.cat_game_backend.dto.response.CatResponse;
import com.catgame.cat_game_backend.dto.response.CatStatusResponse;
import com.catgame.cat_game_backend.exception.CustomException;
import com.catgame.cat_game_backend.exception.ErrorCode;
import com.catgame.cat_game_backend.repository.CatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CatServiceTest {

    @InjectMocks
    private CatService catService;

    @Mock
    private CatRepository catRepository;

    @Mock
    private UserService userService;

    private User createUser() {
        User user = User.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .nickname("tester")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private Cat createCat(User owner) {
        Cat cat = Cat.builder()
                .name("나비")
                .owner(owner)
                .build();
        ReflectionTestUtils.setField(cat, "id", 1L);
        return cat;
    }

    @Test
    @DisplayName("getMyCats - 성공")
    void getMyCats_성공() {
        User owner = createUser();
        Cat cat1 = createCat(owner);
        Cat cat2 = Cat.builder().name("흰둥이").owner(owner).build();
        ReflectionTestUtils.setField(cat2, "id", 2L);

        given(catRepository.findAllByOwnerId(1L)).willReturn(List.of(cat1, cat2));

        List<CatResponse> result = catService.getMyCats(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("나비");
        assertThat(result.get(1).getName()).isEqualTo("흰둥이");
    }

    @Test
    @DisplayName("createCat - 성공")
    void createCat_성공() {
        User owner = createUser();
        CatCreateRequest request = new CatCreateRequest();
        ReflectionTestUtils.setField(request, "name", "나비");

        Cat savedCat = createCat(owner);

        given(catRepository.countByOwnerId(1L)).willReturn(0L);
        given(userService.findById(1L)).willReturn(owner);
        given(catRepository.save(any(Cat.class))).willReturn(savedCat);

        CatResponse response = catService.createCat(request, 1L);

        assertThat(response.getName()).isEqualTo("나비");
    }

    @Test
    @DisplayName("createCat - 5마리 초과 실패")
    void createCat_5마리초과_실패() {
        CatCreateRequest request = new CatCreateRequest();
        ReflectionTestUtils.setField(request, "name", "나비");

        given(catRepository.countByOwnerId(1L)).willReturn(5L);

        assertThatThrownBy(() -> catService.createCat(request, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.CAT_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("feed - 성공")
    void feed_성공() {
        User owner = createUser();
        Cat cat = createCat(owner);
        int initialHunger = cat.getHunger();

        given(catRepository.findByIdAndOwnerId(1L, 1L)).willReturn(Optional.of(cat));

        CatResponse response = catService.feed(1L, 1L);

        assertThat(response.getHunger()).isEqualTo(Math.min(100, initialHunger + 30));
    }

    @Test
    @DisplayName("getCat - 다른 유저 실패")
    void getCat_다른유저_실패() {
        given(catRepository.findByIdAndOwnerId(1L, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> catService.getCat(1L, 2L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.CAT_NOT_FOUND));
    }

    @Test
    @DisplayName("getStatus - 성공")
    void getStatus_성공() {
        User owner = createUser();
        Cat cat = createCat(owner);

        given(catRepository.findByIdAndOwnerId(1L, 1L)).willReturn(Optional.of(cat));

        CatStatusResponse response = catService.getStatus(1L, 1L);

        assertThat(response.getName()).isEqualTo("나비");
        assertThat(response.getHunger()).isEqualTo(80);
        assertThat(response.isNeedsFeeding()).isFalse();
        assertThat(response.isAlert()).isFalse();
    }
}
