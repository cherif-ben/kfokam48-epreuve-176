package com.kfokam48.backend.service;

import com.kfokam48.backend.dto.PromotionResponse;
import com.kfokam48.backend.erreur.PromotionInconnueException;
import com.kfokam48.backend.repository.PromotionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lecture du référentiel des promotions (alimente les sélecteurs du frontend). */
@Service
public class PromotionService {

  private final PromotionRepository promotionRepository;

  public PromotionService(PromotionRepository promotionRepository) {
    this.promotionRepository = promotionRepository;
  }

  @Transactional(readOnly = true)
  public List<PromotionResponse> lister() {
    return promotionRepository.findAllByOrderByNomAsc().stream()
        .map(PromotionResponse::depuis)
        .toList();
  }

  @Transactional(readOnly = true)
  public void verifierExistence(Long promotionId) {
    if (!promotionRepository.existsById(promotionId)) {
      throw new PromotionInconnueException();
    }
  }
}
