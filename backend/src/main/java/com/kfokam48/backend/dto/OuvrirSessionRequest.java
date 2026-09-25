package com.kfokam48.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * EF1 — corps de {@code POST /api/sessions}, exactement le schéma imposé par le contrat
 * ({@code titre} + {@code promotionId} requis).
 */
public record OuvrirSessionRequest(
    @NotBlank(message = "le titre est obligatoire") String titre,
    @NotNull(message = "la promotion est obligatoire") Long promotionId) {}
