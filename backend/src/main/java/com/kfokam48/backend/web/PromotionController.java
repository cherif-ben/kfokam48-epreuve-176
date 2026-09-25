package com.kfokam48.backend.web;

import com.kfokam48.backend.dto.PromotionResponse;
import com.kfokam48.backend.service.PromotionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Référentiel des promotions, utilisé par les sélecteurs du frontend (EF1, EF2). */
@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

  private final PromotionService promotionService;

  public PromotionController(PromotionService promotionService) {
    this.promotionService = promotionService;
  }

  @GetMapping
  public List<PromotionResponse> lister() {
    return promotionService.lister();
  }
}
