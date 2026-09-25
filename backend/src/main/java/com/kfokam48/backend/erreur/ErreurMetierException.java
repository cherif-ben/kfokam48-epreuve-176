package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/**
 * B4 — exception métier : elle porte le code d'erreur stable du contrat et le statut HTTP associé.
 * {@code GestionnaireErreurs} la convertit en {@link ErreurResponse} ; aucun message technique
 * (SQL, stack trace) n'est exposé au client.
 */
public abstract class ErreurMetierException extends RuntimeException {

  private final String code;
  private final HttpStatus statut;

  protected ErreurMetierException(String code, HttpStatus statut, String message) {
    super(message);
    this.code = code;
    this.statut = statut;
  }

  public String getCode() {
    return code;
  }

  public HttpStatus getStatut() {
    return statut;
  }
}
