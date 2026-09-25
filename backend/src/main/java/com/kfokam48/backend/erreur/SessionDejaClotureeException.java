package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** EF11 — la session est déjà clôturée (contrat : 409 SESSION_DEJA_CLOTUREE). */
public class SessionDejaClotureeException extends ErreurMetierException {

  public SessionDejaClotureeException() {
    super("SESSION_DEJA_CLOTUREE", HttpStatus.CONFLICT, "Cette session est déjà clôturée.");
  }
}
