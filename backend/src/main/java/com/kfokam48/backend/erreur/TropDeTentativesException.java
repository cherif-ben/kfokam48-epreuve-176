package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/**
 * RG13 (Q4) — cinq échecs consécutifs sur un couple (étudiant, session) bloquent les tentatives
 * pendant deux minutes.
 */
public class TropDeTentativesException extends ErreurMetierException {

  public TropDeTentativesException(long secondesRestantes) {
    super(
        "TROP_DE_TENTATIVES",
        HttpStatus.TOO_MANY_REQUESTS,
        "Trop de tentatives incorrectes. Réessayez dans %d seconde(s).".formatted(secondesRestantes));
  }
}
