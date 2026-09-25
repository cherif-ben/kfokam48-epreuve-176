package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** EF6 / RG9 — un seul exercice par couple (session, étudiant) (contrat : 409 EXERCICE_DEJA_DEPOSE). */
public class ExerciceDejaDeposeException extends ErreurMetierException {

  public ExerciceDejaDeposeException() {
    super("EXERCICE_DEJA_DEPOSE", HttpStatus.CONFLICT, "Un exercice a déjà été déposé pour cette session.");
  }
}
