package com.kfokam48.backend.erreur;

/**
 * Format d'erreur imposé par {@code api/contrat.yaml} : un code stable en majuscules et une phrase
 * lisible en français. C'est le seul corps d'erreur que l'API renvoie (B4).
 */
public record ErreurResponse(String code, String message) {}
