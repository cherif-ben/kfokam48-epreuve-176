package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** EF7 / RG10 (Q13) — le lien n'est plus modifiable : la relecture a commencé (contrat : 409). */
public class RelectureDejaCommenceeException extends ErreurMetierException {

  public RelectureDejaCommenceeException() {
    super(
        "RELECTURE_DEJA_COMMENCEE",
        HttpStatus.CONFLICT,
        "La relecture a déjà commencé : le lien de l'exercice n'est plus modifiable.");
  }
}
