package com.catgame.cat_game_backend.repository;

import com.catgame.cat_game_backend.domain.entity.Cat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CatRepository extends JpaRepository<Cat, Long> {
    List<Cat> findAllByOwnerId(Long ownerId);
    Optional<Cat> findByIdAndOwnerId(Long id, Long ownerId);
    long countByOwnerId(Long ownerId);
}
