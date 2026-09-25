package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** EF9 — l'appelant n'est pas le relecteur assigné à cette relecture (contrat : 403). */
public class RelecteurNonAssigneException extends ErreurMetierException {

  public RelecteurNonAssigneException() {
    super("RELECTEUR_NON_ASSIGNE", HttpStatus.FORBIDDEN, "Cette relecture n'est pas assignée à cet étudiant.");
  }
}
