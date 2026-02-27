package com.catgame.cat_game_backend.dto.response;

import com.catgame.cat_game_backend.domain.entity.Cat;
import com.catgame.cat_game_backend.domain.enums.CatAge;
import com.catgame.cat_game_backend.domain.enums.HealthStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CatResponse {
    private Long id;
    private String name;
    private int hunger;
    private int thirst;
    private int cleanliness;
    private int happiness;
    private CatAge age;
    private HealthStatus health;
    private LocalDateTime lastFed;
    private LocalDateTime lastWatered;
    private LocalDateTime lastCleaned;
    private LocalDateTime createdAt;

    public static CatResponse from(Cat cat) {
        return CatResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .hunger(cat.getHunger())
                .thirst(cat.getThirst())
                .cleanliness(cat.getCleanliness())
                .happiness(cat.getHappiness())
                .age(cat.getAge())
                .health(cat.getHealth())
                .lastFed(cat.getLastFed())
                .lastWatered(cat.getLastWatered())
                .lastCleaned(cat.getLastCleaned())
                .createdAt(cat.getCreatedAt())
                .build();
    }
}
