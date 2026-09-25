package com.kfokam48.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kfokam48.backend.entity.Session;
import com.kfokam48.backend.repository.PromotionRepository;
import com.kfokam48.backend.repository.SessionRepository;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** EF1 / RG1 — test d'intégration de {@code POST /api/sessions} (201, 404, 400) et du détail. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SessionControllerIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private PromotionRepository promotionRepository;
  @Autowired private SessionRepository sessionRepository;

  private Long promotionId() {
    return promotionRepository.findAllByOrderByNomAsc().get(0).getId();
  }

  @Test
  void ouvrir_valide_retourne201AvecUnCodeDeSixCaracteresEtQuinzeMinutesDeValidite()
      throws Exception {
    String corps = "{\"titre\":\"Cours du jour\",\"promotionId\":%d}".formatted(promotionId());

    mockMvc
        .perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON).content(corps))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.code").isNotEmpty())
        .andExpect(jsonPath("$.ouvertureAt").isNotEmpty())
        .andExpect(jsonPath("$.expirationAt").isNotEmpty());

    Session enregistree = sessionRepository.findAllByOrderByOuvertureAtDesc().get(0);
    assertThat(enregistree.getCode()).hasSize(6);
    // RG1 (Q2) : la durée de validité est calculée côté API, jamais côté front.
    assertThat(Duration.between(enregistree.getOuvertureAt(), enregistree.getExpirationAt()))
        .isEqualTo(Duration.ofMinutes(15));
    assertThat(enregistree.getClotureAt()).isNull();
  }

  @Test
  void ouvrir_avecPromotionInconnue_retourne404PromotionInconnue() throws Exception {
    mockMvc
        .perform(
            post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titre\":\"Cours\",\"promotionId\":999999}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  void ouvrir_sansTitre_retourne400ChampManquant() throws Exception {
    mockMvc
        .perform(
            post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"promotionId\":%d}".formatted(promotionId())))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CHAMP_MANQUANT"));
  }

  @Test
  void detail_sessionInconnue_retourne404() throws Exception {
    mockMvc
        .perform(get("/api/sessions/999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
  }

  @Test
  void lister_parPromotion_retourneLesSessionsPourLeFront() throws Exception {
    mockMvc
        .perform(get("/api/sessions").param("promotionId", String.valueOf(promotionId())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].code").exists())
        .andExpect(jsonPath("$[0].titre").exists());
  }
}
