package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** EF2 — l'identifiant d'étudiant fourni n'existe pas. */
public class EtudiantInconnuException extends ErreurMetierException {

  public EtudiantInconnuException() {
    super("ETUDIANT_INCONNU", HttpStatus.NOT_FOUND, "Cet étudiant n'existe pas.");
  }
}
