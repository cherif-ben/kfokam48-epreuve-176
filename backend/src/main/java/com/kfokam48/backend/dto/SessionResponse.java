package com.kfokam48.backend.dto;

import java.time.LocalDateTime;

/** Réponse de {@code POST /api/sessions} — schéma imposé : id, code, ouvertureAt, expirationAt. */
public record SessionResponse(
    Long id, String code, LocalDateTime ouvertureAt, LocalDateTime expirationAt) {}
