package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** EF2 / RG12 — l'étudiant a déjà marqué sa présence pour cette session (contrat : 409 DEJA_PRESENT). */
public class DejaPresentException extends ErreurMetierException {

  public DejaPresentException() {
    super("DEJA_PRESENT", HttpStatus.CONFLICT, "Cet étudiant a déjà marqué sa présence pour cette session.");
  }
}
