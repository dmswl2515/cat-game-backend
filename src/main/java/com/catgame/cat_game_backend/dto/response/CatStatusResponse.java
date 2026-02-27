package com.catgame.cat_game_backend.dto.response;

import com.catgame.cat_game_backend.domain.entity.Cat;
import com.catgame.cat_game_backend.domain.enums.HealthStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CatStatusResponse {
    private Long id;
    private String name;
    private int hunger;
    private int thirst;
    private int cleanliness;
    private int happiness;
    private HealthStatus health;
    private boolean needsFeeding;
    private boolean needsWater;
    private boolean needsCleaning;
    private boolean alert;

    public static CatStatusResponse from(Cat cat) {
        boolean needsFeeding = cat.getHunger() < 30;
        boolean needsWater = cat.getThirst() < 30;
        boolean needsCleaning = cat.getCleanliness() < 30;
        boolean alert = needsFeeding || needsWater || needsCleaning
                || cat.getHealth() != HealthStatus.HEALTHY;

        return CatStatusResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .hunger(cat.getHunger())
                .thirst(cat.getThirst())
                .cleanliness(cat.getCleanliness())
                .happiness(cat.getHappiness())
                .health(cat.getHealth())
                .needsFeeding(needsFeeding)
                .needsWater(needsWater)
                .needsCleaning(needsCleaning)
                .alert(alert)
                .build();
    }
}
