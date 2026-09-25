package com.kfokam48.backend.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.Relecture;
import com.kfokam48.backend.entity.Session;
import com.kfokam48.backend.entity.StatutExercice;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.ExerciceRepository;
import com.kfokam48.backend.repository.PromotionRepository;
import com.kfokam48.backend.repository.RelectureRepository;
import com.kfokam48.backend.repository.SessionRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** EF8 / RG5 — intégration de {@code GET /api/relectures?relecteurId=} : 200 (avec et sans filtre). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RelectureControllerListeIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private PromotionRepository promotionRepository;
  @Autowired private SessionRepository sessionRepository;
  @Autowired private EtudiantRepository etudiantRepository;
  @Autowired private ExerciceRepository exerciceRepository;
  @Autowired private RelectureRepository relectureRepository;

  @Test
  void lister_relecturesAssignees_retourneLaListeAvecLeLienEtSansIdentite() throws Exception {
    Long promotionId = promotionRepository.findAllByOrderByNomAsc().get(0).getId();
    var etudiants = etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId);
    Long auteurId = etudiants.get(5).getId();
    Long relecteurId = etudiants.get(6).getId();

    Session session =
        new Session("IT relectures", 1L, 1L, "REL001", LocalDateTime.now(), LocalDateTime.now().plusMinutes(30));
    Long sessionId = sessionRepository.save(session).getId();

    Exercice exercice =
        new Exercice(
            sessionId, auteurId, "https://exemple.test/a-relire", StatutExercice.EN_ATTENTE_RELECTURE,
            LocalDateTime.now());
    Long exerciceId = exerciceRepository.save(exercice).getId();
    relectureRepository.save(new Relecture(exerciceId, relecteurId, LocalDateTime.now()));

    mockMvc
        .perform(get("/api/relectures").param("relecteurId", String.valueOf(relecteurId)))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath(
                "$[?(@.exerciceId == " + exerciceId + ")]",
                org.hamcrest.Matchers.hasSize(1)))
        .andExpect(
            jsonPath("$[?(@.exerciceId == " + exerciceId + ")].lienExercice")
                .value(org.hamcrest.Matchers.hasItem("https://exemple.test/a-relire")))
        .andExpect(
            jsonPath("$[?(@.exerciceId == " + exerciceId + ")].statut")
                .value(org.hamcrest.Matchers.hasItem("EN_ATTENTE")))
        // RG6 (Q8) : ni l'auteur ni le relecteur ne sont exposés.
        .andExpect(jsonPath("$[0].relecteurId").doesNotExist())
        .andExpect(jsonPath("$[0].auteurId").doesNotExist());
  }

  @Test
  void lister_avecFiltreStatut_neRetourneQueLeStatutDemande() throws Exception {
    Long promotionId = promotionRepository.findAllByOrderByNomAsc().get(0).getId();
    var etudiants = etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId);
    Long auteurId = etudiants.get(7).getId();
    Long relecteurId = etudiants.get(8).getId();

    Session session =
        new Session("IT relectures 2", 1L, 1L, "REL002", LocalDateTime.now(), LocalDateTime.now().plusMinutes(30));
    Long sessionId = sessionRepository.save(session).getId();

    Exercice exercice =
        new Exercice(
            sessionId, auteurId, "https://exemple.test/rendue", StatutExercice.EN_ATTENTE_RELECTURE,
            LocalDateTime.now());
    Long exerciceId = exerciceRepository.save(exercice).getId();
    Relecture relecture = new Relecture(exerciceId, relecteurId, LocalDateTime.now());
    relecture.rendre(13, "correct", LocalDateTime.now());
    relectureRepository.save(relecture);

    // Filtre EN_ATTENTE : la relecture RENDUE ne doit pas apparaître.
    mockMvc
        .perform(
            get("/api/relectures")
                .param("relecteurId", String.valueOf(relecteurId))
                .param("statut", "EN_ATTENTE"))
        .andExpect(
            jsonPath(
                "$[?(@.exerciceId == " + exerciceId + ")]",
                org.hamcrest.Matchers.hasSize(0)));

    // Filtre RENDUE : elle apparaît avec sa note.
    mockMvc
        .perform(
            get("/api/relectures")
                .param("relecteurId", String.valueOf(relecteurId))
                .param("statut", "RENDUE"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$[?(@.exerciceId == " + exerciceId + ")].note")
                .value(org.hamcrest.Matchers.hasItem(13)));
  }

  @Test
  void lister_sansRelecteurId_retourne400() throws Exception {
    mockMvc
        .perform(get("/api/relectures"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CHAMP_MANQUANT"));
  }
}
