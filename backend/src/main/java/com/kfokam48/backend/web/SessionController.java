package com.kfokam48.backend.web;

import com.kfokam48.backend.dto.OuvrirSessionRequest;
import com.kfokam48.backend.dto.SessionDetailResponse;
import com.kfokam48.backend.dto.SessionResponse;
import com.kfokam48.backend.service.SessionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** EF1 — contrôleur des sessions (B3 : aucune entité JPA ne sort du contrôleur). */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

  private final SessionService sessionService;

  public SessionController(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SessionResponse ouvrir(@Valid @RequestBody OuvrirSessionRequest requete) {
    return sessionService.ouvrir(requete);
  }

  @GetMapping("/{id}")
  public SessionDetailResponse trouver(@PathVariable Long id) {
    return sessionService.trouver(id);
  }

  @GetMapping
  public List<SessionDetailResponse> lister(
      @RequestParam(required = false) Long promotionId,
      @RequestParam(required = false) Long formateurId) {
    return sessionService.lister(promotionId, formateurId);
  }
}
