package com.catgame.cat_game_backend.domain.entity;

import com.catgame.cat_game_backend.domain.enums.HealthStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CatTest {

    private Cat cat;

    @BeforeEach
    void setUp() {
        User owner = User.builder()
                .email("test@test.com")
                .password("password123")
                .nickname("tester")
                .build();

        cat = Cat.builder()
                .name("나비")
                .owner(owner)
                .build();
    }

    @Test
    @DisplayName("feed() - hunger +30 (max 100), happiness +5, lastFed 갱신")
    void feed_증가_및_최대값_제한() {
        int initialHunger = cat.getHunger();
        int initialHappiness = cat.getHappiness();

        cat.feed();

        assertThat(cat.getHunger()).isEqualTo(Math.min(100, initialHunger + 30));
        assertThat(cat.getHappiness()).isEqualTo(Math.min(100, initialHappiness + 5));
        assertThat(cat.getLastFed()).isNotNull();
    }

    @Test
    @DisplayName("feed() - hunger가 100을 초과하지 않음")
    void feed_최대값_초과하지_않음() {
        cat.feed(); // 80 -> 100
        cat.feed(); // 100 -> 100 (capped)

        assertThat(cat.getHunger()).isEqualTo(100);
    }

    @Test
    @DisplayName("water() - thirst +30 (max 100), happiness +5, lastWatered 갱신")
    void water_증가_및_최대값_제한() {
        int initialThirst = cat.getThirst();
        int initialHappiness = cat.getHappiness();

        cat.water();

        assertThat(cat.getThirst()).isEqualTo(Math.min(100, initialThirst + 30));
        assertThat(cat.getHappiness()).isEqualTo(Math.min(100, initialHappiness + 5));
        assertThat(cat.getLastWatered()).isNotNull();
    }

    @Test
    @DisplayName("water() - thirst가 100을 초과하지 않음")
    void water_최대값_초과하지_않음() {
        cat.water(); // 80 -> 100
        cat.water(); // 100 -> 100

        assertThat(cat.getThirst()).isEqualTo(100);
    }

    @Test
    @DisplayName("clean() - cleanliness +30 (max 100), happiness +5, lastCleaned 갱신")
    void clean_증가_및_최대값_제한() {
        int initialCleanliness = cat.getCleanliness();
        int initialHappiness = cat.getHappiness();

        cat.clean();

        assertThat(cat.getCleanliness()).isEqualTo(Math.min(100, initialCleanliness + 30));
        assertThat(cat.getHappiness()).isEqualTo(Math.min(100, initialHappiness + 5));
        assertThat(cat.getLastCleaned()).isNotNull();
    }

    @Test
    @DisplayName("clean() - cleanliness가 100을 초과하지 않음")
    void clean_최대값_초과하지_않음() {
        cat.clean(); // 80 -> 100
        cat.clean(); // 100 -> 100

        assertThat(cat.getCleanliness()).isEqualTo(100);
    }

    @Test
    @DisplayName("decayStats() - hunger -5, thirst -7, cleanliness -3, happiness -4")
    void decayStats_감소() {
        int initialHunger = cat.getHunger();
        int initialThirst = cat.getThirst();
        int initialCleanliness = cat.getCleanliness();
        int initialHappiness = cat.getHappiness();

        cat.decayStats();

        assertThat(cat.getHunger()).isEqualTo(initialHunger - 5);
        assertThat(cat.getThirst()).isEqualTo(initialThirst - 7);
        assertThat(cat.getCleanliness()).isEqualTo(initialCleanliness - 3);
        assertThat(cat.getHappiness()).isEqualTo(initialHappiness - 4);
    }

    @Test
    @DisplayName("decayStats() - 스탯이 0 미만으로 떨어지지 않음")
    void decayStats_최소값_보장() {
        // 스탯을 0으로 만들기 위해 여러 번 decay (cleanliness: 80/3 = 27회 필요)
        for (int i = 0; i < 30; i++) {
            cat.decayStats();
        }

        assertThat(cat.getHunger()).isEqualTo(0);
        assertThat(cat.getThirst()).isEqualTo(0);
        assertThat(cat.getCleanliness()).isEqualTo(0);
        assertThat(cat.getHappiness()).isEqualTo(0);
    }

    @Test
    @DisplayName("recalculateHealth() - avg >= 50 → HEALTHY")
    void recalculateHealth_HEALTHY() {
        // 초기값 80,80,80,80 → avg=80 → HEALTHY
        cat.recalculateHealth();

        assertThat(cat.getHealth()).isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    @DisplayName("recalculateHealth() - 25 <= avg < 50 → SICK")
    void recalculateHealth_SICK() {
        // decay를 여러번 돌려서 avg를 25~50 사이로
        for (int i = 0; i < 8; i++) {
            cat.decayStats();
        }
        // hunger: 80-40=40, thirst: 80-56=24, cleanliness: 80-24=56, happiness: 80-32=48
        // avg = (40+24+56+48)/4 = 42 → SICK

        assertThat(cat.getHealth()).isEqualTo(HealthStatus.SICK);
    }

    @Test
    @DisplayName("recalculateHealth() - avg < 25 → CRITICAL")
    void recalculateHealth_CRITICAL() {
        // decay를 많이 돌려서 avg < 25
        for (int i = 0; i < 16; i++) {
            cat.decayStats();
        }
        // 모든 스탯이 매우 낮아짐 → CRITICAL

        assertThat(cat.getHealth()).isEqualTo(HealthStatus.CRITICAL);
    }
}
