package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.Promotion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux promotions (B3). */
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

  List<Promotion> findAllByOrderByNomAsc();
}
