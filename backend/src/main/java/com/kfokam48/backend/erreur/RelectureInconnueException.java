package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** EF9 — la relecture visée n'existe pas. */
public class RelectureInconnueException extends ErreurMetierException {

  public RelectureInconnueException() {
    super("RELECTURE_INCONNUE", HttpStatus.NOT_FOUND, "Cette relecture n'existe pas.");
  }
}
