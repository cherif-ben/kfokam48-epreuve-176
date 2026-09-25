package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** EF8 / RG2 (Q5) — un étudiant ne peut pas relire son propre exercice (contrat : 403 AUTO_RELECTURE). */
public class AutoRelectureException extends ErreurMetierException {

  public AutoRelectureException() {
    super("AUTO_RELECTURE", HttpStatus.FORBIDDEN, "Un étudiant ne peut pas relire son propre exercice.");
  }
}
