package com.kfokam48.backend.dto;

import com.kfokam48.backend.entity.Presence;
import com.kfokam48.backend.entity.SourcePresence;

/**
 * Réponse de {@code POST /api/presences} — schéma imposé :
 * {@code id, sessionId, etudiantId, source}.
 */
public record PresenceResponse(Long id, Long sessionId, Long etudiantId, SourcePresence source) {

  public static PresenceResponse depuis(Presence presence) {
    return new PresenceResponse(
        presence.getId(), presence.getSessionId(), presence.getEtudiantId(), presence.getSource());
  }
}
