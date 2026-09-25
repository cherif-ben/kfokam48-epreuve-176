package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** EF6 / RG9 — le lien de l'exercice n'est pas une URL exploitable (contrat : 400 LIEN_INVALIDE). */
public class LienInvalideException extends ErreurMetierException {

  public LienInvalideException() {
    super(
        "LIEN_INVALIDE",
        HttpStatus.BAD_REQUEST,
        "Le lien de l'exercice doit être une URL valide (par exemple https://…).");
  }
}
