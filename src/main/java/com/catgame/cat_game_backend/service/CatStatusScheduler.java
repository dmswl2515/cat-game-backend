package com.catgame.cat_game_backend.service;

import com.catgame.cat_game_backend.domain.entity.Cat;
import com.catgame.cat_game_backend.repository.CatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatStatusScheduler {

    private final CatRepository catRepository;

    @Scheduled(fixedRate = 1800000) // 30분마다
    @Transactional
    public void decayAllCatStats() {
        List<Cat> cats = catRepository.findAll();
        for (Cat cat : cats) {
            cat.decayStats();
        }
        if (!cats.isEmpty()) {
            log.info("{}마리 고양이 스탯 감소 완료", cats.size());
        }
    }
}
