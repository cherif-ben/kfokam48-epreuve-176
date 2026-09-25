package com.kfokam48.backend.web;

import com.kfokam48.backend.dto.EtudiantResponse;
import com.kfokam48.backend.service.EtudiantService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Référentiel des étudiants d'une promotion (EF2 : sélection de l'étudiant qui marque sa présence). */
@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

  private final EtudiantService etudiantService;

  public EtudiantController(EtudiantService etudiantService) {
    this.etudiantService = etudiantService;
  }

  @GetMapping
  public List<EtudiantResponse> lister(@RequestParam Long promotionId) {
    return etudiantService.listerParPromotion(promotionId);
  }
}
