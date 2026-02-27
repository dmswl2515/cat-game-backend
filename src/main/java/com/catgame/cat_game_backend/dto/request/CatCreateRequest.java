package com.catgame.cat_game_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CatCreateRequest {

    @NotBlank(message = "고양이 이름은 필수입니다.")
    @Size(min = 1, max = 20, message = "고양이 이름은 1~20자입니다.")
    private String name;
}
