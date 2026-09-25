package com.kfokam48.backend.web;

import com.kfokam48.backend.dto.DeposerExerciceRequest;
import com.kfokam48.backend.dto.ExerciceCompletResponse;
import com.kfokam48.backend.dto.ExerciceResponse;
import com.kfokam48.backend.dto.RemplacerLienRequest;
import com.kfokam48.backend.service.ExerciceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** EF6 / EF7 — contrôleur des exercices (B3). */
@RestController
@RequestMapping("/api/exercices")
public class ExerciceController {

  private final ExerciceService exerciceService;

  public ExerciceController(ExerciceService exerciceService) {
    this.exerciceService = exerciceService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ExerciceResponse deposer(@Valid @RequestBody DeposerExerciceRequest requete) {
    return exerciceService.deposer(requete);
  }

  @PutMapping("/{id}")
  public ExerciceResponse remplacerLien(
      @PathVariable Long id, @Valid @RequestBody RemplacerLienRequest requete) {
    return exerciceService.remplacerLien(id, requete.lien());
  }

  @GetMapping
  public List<ExerciceCompletResponse> lister(
      @RequestParam Long etudiantId, @RequestParam(required = false) Long sessionId) {
    return exerciceService.listerPourEtudiant(etudiantId, sessionId);
  }
}
