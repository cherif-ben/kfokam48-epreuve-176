package com.kfokam48.backend.dto;

import com.kfokam48.backend.entity.Session;
import java.time.LocalDateTime;

/**
 * Détail d'une session — {@code GET /api/sessions/{id}} et {@code GET /api/sessions}.
 * Aucune entité JPA n'est exposée en JSON (B3).
 */
public record SessionDetailResponse(
    Long id,
    String titre,
    Long promotionId,
    Long formateurId,
    String code,
    LocalDateTime ouvertureAt,
    LocalDateTime expirationAt,
    LocalDateTime clotureAt) {

  public static SessionDetailResponse depuis(Session session) {
    return new SessionDetailResponse(
        session.getId(),
        session.getTitre(),
        session.getPromotionId(),
        session.getFormateurId(),
        session.getCode(),
        session.getOuvertureAt(),
        session.getExpirationAt(),
        session.getClotureAt());
  }
}
