package com.kfokam48.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Session;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.PresenceRepository;
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

/** EF2 / EF3 / EF4 — intégration de {@code POST /api/presences} : 201, 410, 400 et 409. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PresenceControllerIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private SessionRepository sessionRepository;
  @Autowired private EtudiantRepository etudiantRepository;
  @Autowired private PresenceRepository presenceRepository;
  @Autowired private PromotionRepository promotionRepository;

  private List<Long> etudiants() {
    Long promotionId = promotionRepository.findAllByOrderByNomAsc().get(0).getId();
    return etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId).stream()
        .map(Etudiant::getId)
        .toList();
  }

  private void creerSession(String code, LocalDateTime expirationAt) {
    sessionRepository.save(new Session("IT présence", 1L, 1L, code, expirationAt.minusMinutes(15), expirationAt));
  }

  @Test
  void marquer_avecCodeValide_retourne201EtSourceEtudiant() throws Exception {
    creerSession("IT16OK", LocalDateTime.now().plusMinutes(10));
    Long etudiantId = etudiants().get(0);

    mockMvc
        .perform(
            post("/api/presences")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"IT16OK\",\"etudiantId\":%d}".formatted(etudiantId)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.etudiantId").value(etudiantId))
        .andExpect(jsonPath("$.sessionId").exists())
        .andExpect(jsonPath("$.source").value("ETUDIANT"));
  }

  @Test
  void marquer_deuxFois_retourne409DejaPresent() throws Exception {
    creerSession("IT16DUP", LocalDateTime.now().plusMinutes(10));
    Long etudiantId = etudiants().get(1);
    String corps = "{\"code\":\"IT16DUP\",\"etudiantId\":%d}".formatted(etudiantId);

    mockMvc
        .perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON).content(corps))
        .andExpect(status().isCreated());

    mockMvc
        .perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON).content(corps))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("DEJA_PRESENT"))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  void marquer_avecCodeExpire_retourne410AuFormatDuContrat() throws Exception {
    creerSession("IT16KO", LocalDateTime.now().minusMinutes(1));
    Long etudiantId = etudiants().get(2);

    mockMvc
        .perform(
            post("/api/presences")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"IT16KO\",\"etudiantId\":%d}".formatted(etudiantId)))
        .andExpect(status().isGone())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code").value("CODE_EXPIRE"))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  void marquer_avecCodeInconnu_retourne400CodeInconnu() throws Exception {
    Long etudiantId = etudiants().get(3);

    mockMvc
        .perform(
            post("/api/presences")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"ZZZ999\",\"etudiantId\":%d}".formatted(etudiantId)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CODE_INCONNU"));
  }

  @Test
  void marquer_apresCloture_retourne410CodeExpire() throws Exception {
    creerSession("IT16CLO", LocalDateTime.now().plusMinutes(10));
    Session session = sessionRepository.findByCode("IT16CLO").orElseThrow();
    session.cloturer(LocalDateTime.now());
    sessionRepository.save(session);
    Long etudiantId = etudiants().get(4);

    mockMvc
        .perform(
            post("/api/presences")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"IT16CLO\",\"etudiantId\":%d}".formatted(etudiantId)))
        .andExpect(status().isGone())
        .andExpect(jsonPath("$.code").value("CODE_EXPIRE"));

    assertThat(presenceRepository.findBySessionId(session.getId())).isEmpty();
  }
}
