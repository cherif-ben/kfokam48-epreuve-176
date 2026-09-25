package com.kfokam48.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * EF5 / RG11 — corps de {@code POST /api/sessions/{id}/presences} : l'étudiant que le formateur
 * ajoute manuellement à la session. La présence est créée avec la source {@code FORMATEUR} (Q14).
 */
public record AjouterPresenceFormateurRequest(
    @NotNull(message = "l'étudiant est obligatoire") Long etudiantId) {}
