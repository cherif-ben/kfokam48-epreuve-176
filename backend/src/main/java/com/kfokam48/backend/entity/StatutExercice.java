package com.kfokam48.backend.entity;

/** D2 / D4 — cycle de vie d'un exercice. */
public enum StatutExercice {
  /** Lien déposé, relecteur en cours d'assignation. */
  DEPOSE,
  /** En attente : aucun relecteur disponible ou relecteur qui n'a pas encore rendu (Q11). */
  EN_ATTENTE_RELECTURE,
  /** Relecture rendue : la note est connue (modifiable jusqu'à la clôture, Q10). */
  RELU
}
