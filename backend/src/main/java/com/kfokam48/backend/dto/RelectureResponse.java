package com.kfokam48.backend.dto;

import com.kfokam48.backend.entity.Relecture;
import com.kfokam48.backend.entity.StatutRelecture;
import java.time.LocalDateTime;

/**
 * EF9 — réponse de {@code POST /api/relectures/{id}}. RG6 (Q8) : le nom du relecteur n'apparaît
 * jamais, seulement l'exercice concerné et le contenu de la relecture.
 */
public record RelectureResponse(
    Long id,
    Long exerciceId,
    Integer note,
    String commentaire,
    StatutRelecture statut,
    LocalDateTime rendueAt) {

  public static RelectureResponse depuis(Relecture relecture) {
    return new RelectureResponse(
        relecture.getId(),
        relecture.getExerciceId(),
        relecture.getNote(),
        relecture.getCommentaire(),
        relecture.getStatut(),
        relecture.getRendueAt());
  }
}