package com.kfokam48.backend.dto;

import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.StatutExercice;

/**
 * Réponse de {@code POST /api/exercices} et de {@code PUT /api/exercices/{id}} — schéma imposé :
 * {@code id} + {@code statut}.
 */
public record ExerciceResponse(Long id, StatutExercice statut) {

  public static ExerciceResponse depuis(Exercice exercice) {
    return new ExerciceResponse(exercice.getId(), exercice.getStatut());
  }
}
