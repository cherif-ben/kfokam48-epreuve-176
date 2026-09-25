package com.kfokam48.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * EF2 — corps de {@code POST /api/presences} : le code de présence et l'étudiant qui marque.
 * Le champ {@code source} (EF5, RG11) est ajouté par le ticket #20.
 */
public record MarquerPresenceRequest(
    @NotBlank(message = "le code de présence est obligatoire") String code,
    @NotNull(message = "l'étudiant est obligatoire") Long etudiantId) {}
