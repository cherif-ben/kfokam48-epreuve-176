package com.kfokam48.backend.web;

import com.kfokam48.backend.dto.RelectureResponse;
import com.kfokam48.backend.dto.RendreRelectureRequest;
import com.kfokam48.backend.service.RelectureService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** EF9 — contrôleur des relectures (B3). */
@RestController
@RequestMapping("/api/relectures")
public class RelectureController {

  private final RelectureService relectureService;

  public RelectureController(RelectureService relectureService) {
    this.relectureService = relectureService;
  }

  /**
   * EF9 / RG2 / RG3 / Q10 — le relecteur rend sa note (0-20) et son commentaire.
   *
   * <p>Le relecteur est identifié par le corps de la requête ({@code relecteurId}), conformément au
   * contrat imposé.
   *
   * @param id       identifiant de la relecture (chemin).
   * @param requete  note, commentaire et relecteurId.
   * @return la relecture mise à jour (sans le nom du relecteur, RG6).
   */
  @PostMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public RelectureResponse rendre(
      @org.springframework.web.bind.annotation.PathVariable Long id,
      @Valid @RequestBody RendreRelectureRequest requete) {
    return relectureService.rendre(id, requete.relecteurId(), requete);
  }
}