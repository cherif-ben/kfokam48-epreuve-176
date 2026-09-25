package com.kfokam48.backend.dto;

import com.kfokam48.backend.entity.Etudiant;

/**
 * EF12 — ligne du tableau récapitulatif (Q14, Q16). Expose les indicateurs clés pour chaque
 * étudiant d'une promotion, sans exposer d'entité JPA (B3). La moyenne des notes est calculée
 * côté API (F3) — le front ne la recalcule jamais.
 */
public record TableauLigneResponse(
    Long etudiantId,
    String nom,
    long presences,
    long exercicesDeposes,
    Double moyenne,
    long relecturesEnAttente) {

  public static TableauLigneResponse depuis(
      Etudiant etudiant,
      long presences,
      long exercicesDeposes,
      Double moyenne,
      long relecturesEnAttente) {
    return new TableauLigneResponse(
        etudiant.getId(),
        etudiant.getNom(),
        presences,
        exercicesDeposes,
        moyenne,
        relecturesEnAttente);
  }
}
