package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/**
 * EF9 / EF10 / RG12 — la note est définitive : la session est clôturée (contrat : 409
 * RELECTURE_DEJA_RENDUE). Décision Q10 &gt; Q15 documentée au §7 du cahier des charges.
 */
public class RelectureDejaRendueException extends ErreurMetierException {

  public RelectureDejaRendueException() {
    super("RELECTURE_DEJA_RENDUE", HttpStatus.CONFLICT, "La session est clôturée : la note est définitive.");
  }
}
