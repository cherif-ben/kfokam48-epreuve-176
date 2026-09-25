package com.kfokam48.backend.service;

import com.kfokam48.backend.dto.EtudiantResponse;
import com.kfokam48.backend.erreur.EtudiantInconnuException;
import com.kfokam48.backend.erreur.PromotionInconnueException;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.PromotionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lecture du référentiel des étudiants (alimente les sélecteurs du frontend, EF2). */
@Service
public class EtudiantService {

  private final EtudiantRepository etudiantRepository;
  private final PromotionRepository promotionRepository;

  public EtudiantService(
      EtudiantRepository etudiantRepository, PromotionRepository promotionRepository) {
    this.etudiantRepository = etudiantRepository;
    this.promotionRepository = promotionRepository;
  }

  @Transactional(readOnly = true)
  public List<EtudiantResponse> listerParPromotion(Long promotionId) {
    if (!promotionRepository.existsById(promotionId)) {
      throw new PromotionInconnueException();
    }
    return etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId).stream()
        .map(EtudiantResponse::depuis)
        .toList();
  }

  @Transactional(readOnly = true)
  public void verifierExistence(Long etudiantId) {
    if (!etudiantRepository.existsById(etudiantId)) {
      throw new EtudiantInconnuException();
    }
  }
}
