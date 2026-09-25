package com.kfokam48.backend.dto;

import com.kfokam48.backend.entity.Etudiant;

/** Étudiant exposé au frontend ({@code GET /api/etudiants}). */
public record EtudiantResponse(Long id, String nom, Long promotionId) {

  public static EtudiantResponse depuis(Etudiant etudiant) {
    return new EtudiantResponse(etudiant.getId(), etudiant.getNom(), etudiant.getPromotionId());
  }
}
