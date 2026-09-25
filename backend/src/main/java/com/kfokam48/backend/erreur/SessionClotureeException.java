package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** EF11 / RG14 — la session est clôturée : l'opération est verrouillée (contrat : 409 SESSION_CLOTUREE). */
public class SessionClotureeException extends ErreurMetierException {

  public SessionClotureeException() {
    super("SESSION_CLOTUREE", HttpStatus.CONFLICT, "La session est clôturée : cette action n'est plus possible.");
  }
}
