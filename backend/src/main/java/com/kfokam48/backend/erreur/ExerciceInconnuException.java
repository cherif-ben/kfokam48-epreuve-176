package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** EF7 — l'exercice visé n'existe pas (contrat : 404 EXERCICE_INCONNU). */
public class ExerciceInconnuException extends ErreurMetierException {

  public ExerciceInconnuException() {
    super("EXERCICE_INCONNU", HttpStatus.NOT_FOUND, "Cet exercice n'existe pas.");
  }
}
