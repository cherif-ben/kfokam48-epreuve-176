package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** B4 — un champ obligatoire est absent ou invalide dans la requête. */
public class ChampManquantException extends ErreurMetierException {

  public ChampManquantException(String message) {
    super("CHAMP_MANQUANT", HttpStatus.BAD_REQUEST, message);
  }
}
