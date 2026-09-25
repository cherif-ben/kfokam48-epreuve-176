package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** EF2 / RG12 — le code de présence saisi ne correspond à aucune session (contrat : 400 CODE_INCONNU). */
public class CodeInconnuException extends ErreurMetierException {

  public CodeInconnuException() {
    super("CODE_INCONNU", HttpStatus.BAD_REQUEST, "Ce code de présence ne correspond à aucune session.");
  }
}
