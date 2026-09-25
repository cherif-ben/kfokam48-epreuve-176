package com.kfokam48.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.Session;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.ExerciceRepository;
import com.kfokam48.backend.repository.PromotionRepository;
import com.kfokam48.backend.repository.SessionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** EF6 / EF7 — intégration de {@code POST /api/exercices} (201, 400, 409) et de {@code PUT}. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExerciceControllerIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private SessionRepository sessionRepository;
  @Autowired private EtudiantRepository etudiantRepository;
  @Autowired private ExerciceRepository exerciceRepository;
  @Autowired private PromotionRepository promotionRepository;

  private Long etudiantId(int index) {
    Long promotionId = promotionRepository.findAllByOrderByNomAsc().get(0).getId();
    List<Etudiant> etudiants = etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId);
    return etudiants.get(index).getId();
  }

  private Long creerSession(String code, boolean cloturee) {
    Session session =
        new Session(
            "IT exercice", 1L, 1L, code, LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
    if (cloturee) {
      session.cloturer(LocalDateTime.now());
    }
    return sessionRepository.save(session).getId();
  }

  private String corps(Long sessionId, Long etudiantId, String lien) {
    return "{\"sessionId\":%d,\"etudiantId\":%d,\"lien\":\"%s\"}"
        .formatted(sessionId, etudiantId, lien);
  }

  @Test
  void deposer_valide_retourne201EnAttenteDeRelecture() throws Exception {
    Long sessionId = creerSession("IT15OK", false);

    mockMvc
        .perform(
            post("/api/exercices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corps(sessionId, etudiantId(0), "https://exemple.test/exercice-1")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.statut").value("EN_ATTENTE_RELECTURE"));

    assertThat(exerciceRepository.findBySessionId(sessionId)).hasSize(1);
  }

  @Test
  void deposer_deuxFois_retourne409ExerciceDejaDepose() throws Exception {
    Long sessionId = creerSession("IT15DUP", false);
    String corps = corps(sessionId, etudiantId(1), "https://exemple.test/exercice-2");

    mockMvc
        .perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON).content(corps))
        .andExpect(status().isCreated());

    mockMvc
        .perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON).content(corps))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("EXERCICE_DEJA_DEPOSE"));
  }

  @Test
  void deposer_avecLienMalForme_retourne400LienInvalide() throws Exception {
    Long sessionId = creerSession("IT15LIEN", false);

    mockMvc
        .perform(
            post("/api/exercices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corps(sessionId, etudiantId(2), "pas une url")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("LIEN_INVALIDE"));
  }

  @Test
  void deposer_apresCloture_retourne409SessionCloturee() throws Exception {
    Long sessionId = creerSession("IT15CLO", true);

    mockMvc
        .perform(
            post("/api/exercices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corps(sessionId, etudiantId(3), "https://exemple.test/exercice-3")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
  }

  @Test
  void remplacerLien_puisRefusQuandExerciceRelu() throws Exception {
    Long sessionId = creerSession("IT15PUT", false);
    Long etudiantId = etudiantId(4);

    mockMvc
        .perform(
            post("/api/exercices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corps(sessionId, etudiantId, "https://exemple.test/v1")))
        .andExpect(status().isCreated());

    Long exerciceId = exerciceRepository.findBySessionId(sessionId).get(0).getId();

    // RG10 : tant que la relecture n'a pas commencé, le lien est modifiable.
    mockMvc
        .perform(
            put("/api/exercices/" + exerciceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lien\":\"https://exemple.test/v2\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statut").value("EN_ATTENTE_RELECTURE"));

    Exercice exercice = exerciceRepository.findById(exerciceId).orElseThrow();
    assertThat(exercice.getLien()).isEqualTo("https://exemple.test/v2");

    exercice.marquerRelu(LocalDateTime.now());
    exerciceRepository.save(exercice);

    mockMvc
        .perform(
            put("/api/exercices/" + exerciceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lien\":\"https://exemple.test/v3\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("RELECTURE_DEJA_COMMENCEE"));
  }
}
