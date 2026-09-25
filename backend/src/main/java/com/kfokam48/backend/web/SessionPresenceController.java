package com.kfokam48.backend.web;

import com.kfokam48.backend.dto.AjouterPresenceFormateurRequest;
import com.kfokam48.backend.dto.PresenceResponse;
import com.kfokam48.backend.service.PresenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** EF5 / RG11 — ajout manuel de présence par le formateur (B3). */
@RestController
@RequestMapping("/api/sessions/{id}/presences")
public class SessionPresenceController {

  private final PresenceService presenceService;

  public SessionPresenceController(PresenceService presenceService) {
    this.presenceService = presenceService;
  }

  /**
   * EF5 / RG11 (Q14) — le formateur ajoute la présence d'un étudiant : 201 avec
   * {@code source = FORMATEUR}. Erreurs : 404 session/étudiant inconnu, 409 DEJA_PRESENT
   * (RG12), 410 SESSION_CLOTUREE (RG14).
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PresenceResponse ajouter(
      @PathVariable Long id, @Valid @RequestBody AjouterPresenceFormateurRequest requete) {
    return presenceService.ajouterPresenceFormateur(id, requete.etudiantId());
  }
}
