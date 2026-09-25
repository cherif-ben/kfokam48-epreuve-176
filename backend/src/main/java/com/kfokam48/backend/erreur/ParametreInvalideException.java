package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** B4 — un paramètre de requête est mal formé (type attendu, valeur d'énumération inconnue…). */
public class ParametreInvalideException extends ErreurMetierException {

  public ParametreInvalideException(String message) {
    super("PARAMETRE_INVALIDE", HttpStatus.BAD_REQUEST, message);
  }
}
