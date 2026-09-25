package com.kfokam48.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
 * EF10 / RG11 (Q10) / RG12 — intégration de la correction de note (second {@code POST
 * /api/relectures/{id}}) : 200 avant clôture, 409 RELECTURE_DEJA_RENDUE après clôture. Décision
 * Q10 &gt; Q15 documentée en §7 du cahier des charges.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorrectionRelectureIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private PromotionRepository promotionRepository;
  @Autowired private SessionRepository sessionRepository;
  @Autowired private EtudiantRepository etudiantRepository;
  @Autowired private ExerciceRepository exerciceRepository;
  @Autowired private RelectureRepository relectureRepository;

  private Long creerSession(String code) {
    Session session =
        new Session("IT correction", 1L, 1L, code, LocalDateTime.now(), LocalDateTime.now().plusMinutes(30));
    return sessionRepository.save(session).getId();
  }

  private Long exerciceDeRelecteur(Long sessionId, Long auteurId, Long relecteurId) {
    Exercice exercice =
        new Exercice(
            sessionId,
            auteurId,
            "https://exemple.test/" + auteurId,
            StatutExercice.EN_ATTENTE_RELECTURE,
            LocalDateTime.now());
    Long exerciceId = exerciceRepository.save(exercice).getId();
    Relecture relecture = new Relecture(exerciceId, relecteurId, LocalDateTime.now());
    relecture.rendre(10, "premiere version", LocalDateTime.now());
    relectureRepository.save(relecture);
    // État après une première reddition passée par le service : l'exercice est RELU (EF9).
    exercice.marquerRelu(LocalDateTime.now());
    exerciceRepository.save(exercice);
    return relecture.getId();
  }

  @Test
  void corriger_avantCloture_retourne200AvecNoteRemplaceeEtModifieeAt() throws Exception {
    Long sessionId = creerSession("COR001");
    var etudiants = etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId());
    Long relectureId = exerciceDeRelecteur(sessionId, etudiants.get(0).getId(), etudiants.get(1).getId());

    // Première reddition déjà posée (note 10). Second appel = correction (EF10, RG11/Q10).
    mockMvc
        .perform(
            post("/api/relectures/{id}", relectureId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"relecteurId\":%d,\"note\":17,\"commentaire\":\"version corrigee\"}"
                        .formatted(etudiants.get(1).getId())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.note").value(17))
        .andExpect(jsonPath("$.commentaire").value("version corrigee"))
        .andExpect(jsonPath("$.statut").value("RENDUE"));

    Relecture enregistree = relectureRepository.findById(relectureId).orElseThrow();
    // RG11 (Q10) : la note est remplacée, modifieeAt est renseigné.
    assertThat(enregistree.getNote()).isEqualTo(17);
    assertThat(enregistree.getModifieeAt()).isNotNull();
    // Le statut de l'exercice reste RELU (aucun retour en arrière).
    assertThat(exerciceRepository.findById(enregistree.getExerciceId()).orElseThrow().getStatut())
        .isEqualTo(StatutExercice.RELU);
  }

  @Test
  void corriger_apresCloture_retourne409RelectureDejaRendue() throws Exception {
    // RG12 : la note devient définitive à la clôture de la session.
    Long sessionId = creerSession("COR002");
    var etudiants = etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId());
    Long relectureId = exerciceDeRelecteur(sessionId, etudiants.get(2).getId(), etudiants.get(3).getId());

    mockMvc
        .perform(patch("/api/sessions/{id}/cloture", sessionId))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/relectures/{id}", relectureId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"relecteurId\":%d,\"note\":18,\"commentaire\":\"trop tard\"}"
                        .formatted(etudiants.get(3).getId())))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("RELECTURE_DEJA_RENDUE"));

    // La note d'origine n'a pas bougé.
    assertThat(relectureRepository.findById(relectureId).orElseThrow().getNote()).isEqualTo(10);
  }

  private Long promotionId() {
    return promotionRepository.findAllByOrderByNomAsc().get(0).getId();
  }
}
