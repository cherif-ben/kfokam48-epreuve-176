package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/**
 * EF3 / RG1 (Q2) — le code de présence a expiré (contrat : 410 CODE_EXPIRE), ou la session est
 * clôturée : RG14 (Q3) impose le même verrou « pas de présence après clôture ».
 */
public class CodeExpireException extends ErreurMetierException {

  public CodeExpireException() {
    super("CODE_EXPIRE", HttpStatus.GONE, "Le code de présence a expiré. Demandez un nouveau code au formateur.");
  }
}
