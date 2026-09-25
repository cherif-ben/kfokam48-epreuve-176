package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.Session;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux sessions (B3 : repository séparé du service). */
public interface SessionRepository extends JpaRepository<Session, Long> {

  Optional<Session> findByCode(String code);

  boolean existsByCode(String code);

  List<Session> findAllByOrderByOuvertureAtDesc();

  List<Session> findByPromotionIdOrderByOuvertureAtDesc(Long promotionId);

  List<Session> findByFormateurIdOrderByOuvertureAtDesc(Long formateurId);

  List<Session> findByPromotionIdAndFormateurIdOrderByOuvertureAtDesc(Long promotionId, Long formateurId);
}
