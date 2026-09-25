package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** EF1 — aucune donnée de formateur n'existe pour rattacher la session. */
public class FormateurInconnuException extends ErreurMetierException {

  public FormateurInconnuException() {
    super("FORMATEUR_INCONNU", HttpStatus.NOT_FOUND, "Aucun formateur n'est enregistré.");
  }
}
