package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/**
 * EF5 / RG14 — ajout manuel de présence sur une session clôturée. Le contrat
 * §/api/sessions/{id}/presences distingue le 410 (session clôturée, comme CODE_EXPIRE pour
 * l'étudiant) du 409 (conflit métier) : cette exception porte le statut 410.
 */
public class PresenceSessionClotureeException extends ErreurMetierException {

  public PresenceSessionClotureeException() {
    super("SESSION_CLOTUREE", HttpStatus.GONE, "La session est clôturée : plus aucune présence ne peut être ajoutée.");
  }
}
