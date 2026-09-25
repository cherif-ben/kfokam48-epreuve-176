package com.kfokam48.backend.entity;

/** D2 / D4 — cycle de vie d'une relecture. */
public enum StatutRelecture {
  /** Relecteur assigné, pas encore rendu (RG8 : l'exercice reste en attente). */
  EN_ATTENTE,
  /** Relecture rendue : note et commentaire disponibles (Q10). */
  RENDUE
}
