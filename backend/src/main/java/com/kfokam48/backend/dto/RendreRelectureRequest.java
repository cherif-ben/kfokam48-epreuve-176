package com.kfokam48.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * EF9 — corps de {@code POST /api/relectures/{id}} : l'identité du relecteur, la note (entier 0-20,
 * RG3) et le commentaire.
 *
 * <p>La validation de la plage 0-20 est effectuée côté métier (service) pour lever la même
 * exception ({@code NOTE_INVALIDE}) que la note non entière (contrat).
 */
public record RendreRelectureRequest(
    @NotNull(message = "le relecteur est obligatoire") Long relecteurId,
    @NotNull(message = "la note est obligatoire") Integer note,
    @NotBlank(message = "le commentaire est obligatoire") String commentaire) {}