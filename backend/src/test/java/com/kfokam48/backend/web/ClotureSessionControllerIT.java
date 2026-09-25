package com.kfokam48.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * EF11 / RG14 (Q3) — intégration de {@code PATCH /api/sessions/{id}/cloture} : 200, 409
 * SESSION_DEJA_CLOTUREE, 404 SESSION_INCONNUE, et le verrouillage post-clôture (présence → 410,
 * dépôt → 409, relecture → 409 RELECTURE_DEJA_RENDUE).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClotureSessionControllerIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private PromotionRepository promotionRepository;
  @Autowired private SessionRepository sessionRepository;
  @Autowired private EtudiantRepository etudiantRepository;
  @Autowired private ExerciceRepository exerciceRepository;
  @Autowired private RelectureRepository relectureRepository;

  private Long promotionId() {
    return promotionRepository.findAllByOrderByNomAsc().get(0).getId();
  }

  private Long creerSession(String code) {
    Session session =
        new Session("IT clôture", 1L, 1L, code, LocalDateTime.now(), LocalDateTime.now().plusMinutes(30));
    return sessionRepository.save(session).getId();
  }

  @Test
  void cloturer_sessionOuverte_retourne200AvecClotureAtRenseigne() throws Exception {
    Long id = creerSession("CLO001");

    mockMvc
        .perform(patch("/api/sessions/{id}/cloture", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.clotureAt").isNotEmpty());

    assertThat(sessionRepository.findById(id).orElseThrow().getClotureAt()).isNotNull();
  }

  @Test
  void cloturer_sessionDejaCloturee_retourne409SessionDejaCloturee() throws Exception {
    Long id = creerSession("CLO002");
    Session cloturee = sessionRepository.findById(id).orElseThrow();
    cloturee.cloturer(LocalDateTime.now());
    sessionRepository.save(cloturee);

    mockMvc
        .perform(patch("/api/sessions/{id}/cloture", id))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SESSION_DEJA_CLOTUREE"))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  void cloturer_sessionInconnue_retourne404SessionInconnue() throws Exception {
    mockMvc
        .perform(patch("/api/sessions/{id}/cloture", 999999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
  }

  @Test
  void presence_apresCloture_retourne410CodeExpire() throws Exception {
    // RG14 (Q3) : après clôture, le marquage de présence est verrouillé.
    Long id = creerSession("CLO003");
    Long etudiantId = etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId()).get(0).getId();
    mockMvc.perform(patch("/api/sessions/{id}/cloture", id)).andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/presences")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"CLO003\",\"etudiantId\":%d}".formatted(etudiantId)))
        .andExpect(status().isGone())
        .andExpect(jsonPath("$.code").value("CODE_EXPIRE"));
  }

  @Test
  void depot_apresCloture_retourne409SessionCloturee() throws Exception {
    // Q12 : le dépôt est possible jusqu'à la clôture — plus après.
    Long id = creerSession("CLO004");
    Long etudiantId = etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId()).get(1).getId();
    mockMvc.perform(patch("/api/sessions/{id}/cloture", id)).andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/exercices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"sessionId\":%d,\"etudiantId\":%d,\"lien\":\"https://exemple.test/x\"}"
                        .formatted(id, etudiantId)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
  }

  @Test
  void relecture_apresCloture_retourne409RelectureDejaRendue() throws Exception {
    // RG12 / Q10 : la note devient définitive à la clôture.
    Long sessionId = creerSession("CLO005");
    Etudiant auteur = etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId()).get(2);
    Etudiant relecteur = etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId()).get(3);

    Exercice exercice =
        new Exercice(
            sessionId,
            auteur.getId(),
            "https://exemple.test/clo",
            StatutExercice.EN_ATTENTE_RELECTURE,
            LocalDateTime.now());
    Long exerciceId = exerciceRepository.save(exercice).getId();
    Relecture relecture = new Relecture(exerciceId, relecteur.getId(), LocalDateTime.now());
    Long relectureId = relectureRepository.save(relecture).getId();

    mockMvc.perform(patch("/api/sessions/{id}/cloture", sessionId)).andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/relectures/{id}", relectureId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"relecteurId\":%d,\"note\":14,\"commentaire\":\"correct\"}"
                        .formatted(relecteur.getId())))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("RELECTURE_DEJA_RENDUE"));
  }

  @Test
  void lien_apresRelu_avantCloture_retourne409RelectureDejaCommencee() throws Exception {
    // Garde-fou : PUT /api/exercices/{id} reste verrouillé dès que l'exercice est RELU.
    Long sessionId = creerSession("CLO006");
    Etudiant auteur = etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId()).get(4);

    Exercice exercice =
        new Exercice(
            sessionId,
            auteur.getId(),
            "https://exemple.test/avant",
            StatutExercice.EN_ATTENTE_RELECTURE,
            LocalDateTime.now());
    Long exerciceId = exerciceRepository.save(exercice).getId();
    exercice.marquerRelu(LocalDateTime.now());
    exerciceRepository.save(exercice);

    mockMvc
        .perform(
            put("/api/exercices/{id}", exerciceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lien\":\"https://exemple.test/apres\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("RELECTURE_DEJA_COMMENCEE"));
  }
}
