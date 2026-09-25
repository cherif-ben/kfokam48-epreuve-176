package com.kfokam48.backend.dto;

import java.time.LocalDateTime;

/**
 * EF11 — réponse de {@code PATCH /api/sessions/{id}/cloture} (contrat §Libre) :
 * l'identifiant de la session et la date de clôture posée.
 */
public record CloturerSessionResponse(Long id, LocalDateTime clotureAt) {

  public static CloturerSessionResponse depuis(Long id, LocalDateTime clotureAt) {
    return new CloturerSessionResponse(id, clotureAt);
  }
}
