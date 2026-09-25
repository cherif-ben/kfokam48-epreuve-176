package com.kfokam48.backend.erreur;

import org.springframework.http.HttpStatus;

/** EF9 / RG3 — la note n'est pas un entier entre 0 et 20 (contrat : 400 NOTE_INVALIDE). */
public class NoteInvalideException extends ErreurMetierException {

  public NoteInvalideException() {
    super("NOTE_INVALIDE", HttpStatus.BAD_REQUEST, "La note doit être un nombre entier compris entre 0 et 20.");
  }
}
