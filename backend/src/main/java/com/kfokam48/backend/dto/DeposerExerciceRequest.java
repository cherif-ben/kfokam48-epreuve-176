package com.kfokam48.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * EF6 — corps de {@code POST /api/exercices} : exactement le schéma imposé
 * ({@code sessionId}, {@code etudiantId}, {@code lien}).
 */
public record DeposerExerciceRequest(
    @NotNull(message = "la session est obligatoire") Long sessionId,
    @NotNull(message = "l'étudiant est obligatoire") Long etudiantId,
    @NotBlank(message = "le lien est obligatoire") String lien) {}
