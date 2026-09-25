package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** EF11 — la session demandée n'existe pas (contrat : 404 SESSION_INCONNUE). */
public class SessionInconnueException extends ErreurMetierException {

  public SessionInconnueException() {
    super("SESSION_INCONNUE", HttpStatus.NOT_FOUND, "Cette session n'existe pas.");
  }
}
