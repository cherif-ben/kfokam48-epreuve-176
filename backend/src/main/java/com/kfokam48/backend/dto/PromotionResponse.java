package com.kfokam48.backend.dto;

import com.kfokam48.backend.entity.Promotion;

/** Promotion exposée au frontend ({@code GET /api/promotions}). */
public record PromotionResponse(Long id, String nom) {

  public static PromotionResponse depuis(Promotion promotion) {
    return new PromotionResponse(promotion.getId(), promotion.getNom());
  }
}
