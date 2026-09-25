package com.kfokam48.backend.dto;

import com.kfokam48.backend.entity.StatutRelecture;

/**
 * Relecture exposée par {@code GET /api/relectures} (EF8) : construite en service pour le
 * relecteur connecté, avec le lien de l'exercice à relire. RG6 (Q8) : aucune identité de
 * relecteur n'est exposée — ni l'auteur, ni le relecteur.
 */
public record RelectureAssigneeResponse(
    Long id,
    Long exerciceId,
    String lienExercice,
    StatutRelecture statut,
    Integer note,
    String commentaire) {

  public static RelectureAssigneeResponse depuis(
      Long id,
      Long exerciceId,
      String lienExercice,
      StatutRelecture statut,
      Integer note,
      String commentaire) {
    return new RelectureAssigneeResponse(id, exerciceId, lienExercice, statut, note, commentaire);
  }
}
