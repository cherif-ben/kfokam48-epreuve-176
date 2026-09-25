package com.kfokam48.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** EF7 — corps de {@code PUT /api/exercices/{id}} : le nouveau lien. */
public record RemplacerLienRequest(@NotBlank(message = "le lien est obligatoire") String lien) {}
