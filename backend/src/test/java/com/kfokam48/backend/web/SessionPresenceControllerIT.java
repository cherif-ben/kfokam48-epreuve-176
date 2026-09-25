package com.kfokam48.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Presence;
import com.kfokam48.backend.entity.Session;
import com.kfokam48.backend.entity.SourcePresence;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.PresenceRepository;
import com.kfokam48.backend.repository.PromotionRepository;
import com.kfokam48.backend.repository.SessionRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** EF5 / RG11 (Q14) — intégration de {@code POST /api/sessions/{id}/presences} : 201, 409, 410. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SessionPresenceControllerIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private PromotionRepository promotionRepository;
  @Autowired private SessionRepository sessionRepository;
  @Autowired private EtudiantRepository etudiantRepository;
  @Autowired private PresenceRepository presenceRepository;

  private Long creerSession(String code) {
    Session session =
        new Session("IT formateur", 1L, 1L, code, LocalDateTime.now(), LocalDateTime.now().plusMinutes(30));
    return sessionRepository.save(session).getId();
  }

  @Test
  void ajouterManuellement_valide_retourne201AvecSourceFormateur() throws Exception {
    Long sessionId = creerSession("FMT001");
    Long etudiantId =
        etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId()).get(0).getId();

    mockMvc
        .perform(
            post("/api/sessions/{id}/presences", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"etudiantId\":%d}".formatted(etudiantId)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.sessionId").value(sessionId))
        .andExpect(jsonPath("$.etudiantId").value(etudiantId))
        .andExpect(jsonPath("$.source").value("FORMATEUR"));

    // RG11 : la présence est tracée avec la source FORMATEUR (Q14).
    Presence enregistree = presenceRepository.findBySessionId(sessionId).get(0);
    assertThat(enregistree.getSource()).isEqualTo(SourcePresence.FORMATEUR);
  }

  @Test
  void ajouterManuellement_etudiantDejaPresent_retourne409DejaPresent() throws Exception {
    // RG12 : l'unicité (session, étudiant) s'applique quelle que soit la source.
    Long sessionId = creerSession("FMT002");
    var etudiants = etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId());
    Long etudiantId = etudiants.get(1).getId();
    presenceRepository.save(
        new Presence(sessionId, etudiantId, SourcePresence.ETUDIANT, LocalDateTime.now()));

    mockMvc
        .perform(
            post("/api/sessions/{id}/presences", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"etudiantId\":%d}".formatted(etudiantId)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("DEJA_PRESENT"));
  }

  @Test
  void ajouterManuellement_sessionCloturee_retourne410SessionCloturee() throws Exception {
    // RG14 : plus aucun ajout après clôture.
    Long sessionId = creerSession("FMT003");
    Session cloturee = sessionRepository.findById(sessionId).orElseThrow();
    cloturee.cloturer(LocalDateTime.now());
    sessionRepository.save(cloturee);
    Long etudiantId =
        etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId()).get(2).getId();

    mockMvc
        .perform(
            post("/api/sessions/{id}/presences", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"etudiantId\":%d}".formatted(etudiantId)))
        .andExpect(status().isGone())
        .andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
  }

  @Test
  void ajouterManuellement_sessionInconnue_retourne404() throws Exception {
    mockMvc
        .perform(
            post("/api/sessions/{id}/presences", 999999)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"etudiantId\":1}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
  }

  @Test
  void ajouterManuellement_etudiantInconnu_retourne404() throws Exception {
    Long sessionId = creerSession("FMT004");
    mockMvc
        .perform(
            post("/api/sessions/{id}/presences", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"etudiantId\":999999}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("ETUDIANT_INCONNU"));
  }

  private Long promotionId() {
    return promotionRepository.findAllByOrderByNomAsc().get(0).getId();
  }
}
