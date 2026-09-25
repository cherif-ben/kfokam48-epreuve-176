package com.kfokam48.backend.dto;

import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.StatutExercice;

/**
 * Vue « côté étudiant » d'un exercice pour {@code GET /api/exercices} (RG6) : jamais l'identité du
 * relecteur, seulement la note et le commentaire reçus (Q8).
 */
public record ExerciceCompletResponse(
    Long id,
    Long sessionId,
    String lien,
    StatutExercice statut,
    Integer note,
    String commentaire) {

  public static ExerciceCompletResponse depuis(
      Exercice exercice, Integer note, String commentaire) {
    return new ExerciceCompletResponse(
        exercice.getId(),
        exercice.getSessionId(),
        exercice.getLien(),
        exercice.getStatut(),
        note,
        commentaire);
  }

  public static ExerciceCompletResponse depuis(Exercice exercice) {
    return depuis(exercice, null, null);
  }
}
