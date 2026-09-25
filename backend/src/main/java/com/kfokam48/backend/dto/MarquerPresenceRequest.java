package com.kfokam48.backend.dto;

import com.kfokam48.backend.entity.SourcePresence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * EF2 / EF5 — corps de {@code POST /api/presences} : le code de présence, l'étudiant qui marque
 * et, optionnellement, la source de la présence (RG11, Q14).
 *
 * <p>Le champ {@code source} est optionnel : absent ou {@code null}, la présence est par défaut
 * {@code ETUDIANT} (auto-déclaration). La valeur {@code FORMATEUR} indique une présence ajoutée
 * manuellement par le formateur (EF5, RG11).
 */
public record MarquerPresenceRequest(
    @NotBlank(message = "le code de présence est obligatoire") String code,
    @NotNull(message = "l'étudiant est obligatoire") Long etudiantId,
    SourcePresence source) {

  public MarquerPresenceRequest(String code, Long etudiantId) {
    this(code, etudiantId, null);
  }
}
