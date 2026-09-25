package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** EF1 / EF12 — la promotion demandée n'existe pas (contrat : 404 PROMOTION_INCONNUE). */
public class PromotionInconnueException extends ErreurMetierException {

  public PromotionInconnueException() {
    super("PROMOTION_INCONNUE", HttpStatus.NOT_FOUND, "Cette promotion n'existe pas.");
  }
}
