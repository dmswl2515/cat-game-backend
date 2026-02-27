package com.catgame.cat_game_backend.domain.entity;

import com.catgame.cat_game_backend.domain.enums.CatAge;
import com.catgame.cat_game_backend.domain.enums.HealthStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int hunger = 80;

    @Column(nullable = false)
    private int thirst = 80;

    @Column(nullable = false)
    private int cleanliness = 80;

    @Column(nullable = false)
    private int happiness = 80;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CatAge age = CatAge.KITTEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HealthStatus health = HealthStatus.HEALTHY;

    private LocalDateTime lastFed;
    private LocalDateTime lastWatered;
    private LocalDateTime lastCleaned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Builder
    public Cat(String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.hunger = 80;
        this.thirst = 80;
        this.cleanliness = 80;
        this.happiness = 80;
        this.age = CatAge.KITTEN;
        this.health = HealthStatus.HEALTHY;
        this.lastFed = LocalDateTime.now();
        this.lastWatered = LocalDateTime.now();
        this.lastCleaned = LocalDateTime.now();
    }

    public void feed() {
        this.hunger = Math.min(100, this.hunger + 30);
        this.happiness = Math.min(100, this.happiness + 5);
        this.lastFed = LocalDateTime.now();
        recalculateHealth();
    }

    public void water() {
        this.thirst = Math.min(100, this.thirst + 30);
        this.happiness = Math.min(100, this.happiness + 5);
        this.lastWatered = LocalDateTime.now();
        recalculateHealth();
    }

    public void clean() {
        this.cleanliness = Math.min(100, this.cleanliness + 30);
        this.happiness = Math.min(100, this.happiness + 5);
        this.lastCleaned = LocalDateTime.now();
        recalculateHealth();
    }

    public void decayStats() {
        this.hunger = Math.max(0, this.hunger - 5);
        this.thirst = Math.max(0, this.thirst - 7);
        this.cleanliness = Math.max(0, this.cleanliness - 3);
        this.happiness = Math.max(0, this.happiness - 4);
        recalculateHealth();
    }

    public void recalculateHealth() {
        double avg = (hunger + thirst + cleanliness + happiness) / 4.0;
        if (avg >= 50) {
            this.health = HealthStatus.HEALTHY;
        } else if (avg >= 25) {
            this.health = HealthStatus.SICK;
        } else {
            this.health = HealthStatus.CRITICAL;
        }
    }
}
