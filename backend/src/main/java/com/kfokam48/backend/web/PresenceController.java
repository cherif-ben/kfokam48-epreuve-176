package com.kfokam48.backend.web;

import com.kfokam48.backend.dto.MarquerPresenceRequest;
import com.kfokam48.backend.dto.PresenceResponse;
import com.kfokam48.backend.service.PresenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** EF2 / EF3 / EF4 — contrôleur des présences (B3). */
@RestController
@RequestMapping("/api/presences")
public class PresenceController {

  private final PresenceService presenceService;

  public PresenceController(PresenceService presenceService) {
    this.presenceService = presenceService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PresenceResponse marquer(@Valid @RequestBody MarquerPresenceRequest requete) {
    return presenceService.marquerParEtudiant(requete);
  }
}
