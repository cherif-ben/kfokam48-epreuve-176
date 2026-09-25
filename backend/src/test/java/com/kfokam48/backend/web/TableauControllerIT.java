package com.kfokam48.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.Presence;
import com.kfokam48.backend.entity.Relecture;
import com.kfokam48.backend.entity.Session;
import com.kfokam48.backend.entity.SourcePresence;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.ExerciceRepository;
import com.kfokam48.backend.repository.PresenceRepository;
import com.kfokam48.backend.repository.PromotionRepository;
import com.kfokam48.backend.repository.RelectureRepository;
import com.kfokam48.backend.repository.SessionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** EF12 — intégration de {@code GET /api/tableau?promotionId=} : 200 et 404. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TableauControllerIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private PromotionRepository promotionRepository;
  @Autowired private EtudiantRepository etudiantRepository;
  @Autowired private SessionRepository sessionRepository;
  @Autowired private PresenceRepository presenceRepository;
  @Autowired private ExerciceRepository exerciceRepository;
  @Autowired private RelectureRepository relectureRepository;

  private Long promotionId() {
    return promotionRepository.findAllByOrderByNomAsc().get(0).getId();
  }

  private List<Etudiant> etudiants(Long promotionId) {
    return etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId);
  }

  private Long creerSession(String code) {
    Session session =
        new Session(
            "IT tableau", 1L, 1L, code, LocalDateTime.now(), LocalDateTime.now().plusMinutes(30));
    return sessionRepository.save(session).getId();
  }

  @Test
  void tableau_promotionValide_retourne200AvecTousLesChamps() throws Exception {
    Long promotionId = promotionId();
    List<Etudiant> etudiants = etudiants(promotionId);

    mockMvc
        .perform(get("/api/tableau").param("promotionId", String.valueOf(promotionId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].etudiantId").exists())
        .andExpect(jsonPath("$[0].nom").exists())
        .andExpect(jsonPath("$[0].presences").value(0))
        .andExpect(jsonPath("$[0].exercicesDeposes").value(0))
        .andExpect(jsonPath("$[0].moyenne").doesNotExist())
        .andExpect(jsonPath("$[0].relecturesEnAttente").value(0))
        .andExpect(jsonPath("$.length()").value(etudiants.size()));
  }

  @Test
  void tableau_promotionInconnue_retourne404PromotionInconnue() throws Exception {
    mockMvc
        .perform(get("/api/tableau").param("promotionId", "999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  void tableau_avecPresencesEtExercisesEtRelectures_aggregueCorrectement() throws Exception {
    Long promotionId = promotionId();
    List<Etudiant> etudiants = etudiants(promotionId);
    Long sessionId = creerSession("ITTAB1");
    Long etudiantId0 = etudiants.get(0).getId();
    Long etudiantId1 = etudiants.get(1).getId();
    Long etudiantId2 = etudiants.get(2).getId();
    Long etudiantId3 = etudiants.get(3).getId();

    presenceRepository.save(
        new Presence(sessionId, etudiantId0, SourcePresence.ETUDIANT, LocalDateTime.now()));

    Exercice exercice1 =
        new Exercice(
            sessionId,
            etudiantId1,
            "https://exemple.test/1",
            com.kfokam48.backend.entity.StatutExercice.EN_ATTENTE_RELECTURE,
            LocalDateTime.now());
    exerciceRepository.save(exercice1);
    Relecture relecture1 = new Relecture(exercice1.getId(), etudiantId2, LocalDateTime.now());
    relecture1.rendre(15, "bien", LocalDateTime.now());
    relectureRepository.save(relecture1);

    Exercice exercice2 =
        new Exercice(
            sessionId,
            etudiantId3,
            "https://exemple.test/2",
            com.kfokam48.backend.entity.StatutExercice.EN_ATTENTE_RELECTURE,
            LocalDateTime.now());
    exerciceRepository.save(exercice2);
    Relecture relecture2 = new Relecture(exercice2.getId(), etudiantId2, LocalDateTime.now());
    relectureRepository.save(relecture2);

    mockMvc
        .perform(get("/api/tableau").param("promotionId", String.valueOf(promotionId)))
        .andExpect(status().isOk())
        // étudiantId0 : 1 présence, 0 exercice, moyenne=null
        .andExpect(jsonPath("$[?(@.etudiantId == " + etudiantId0 + ")][0].presences").value(1))
        .andExpect(jsonPath("$[?(@.etudiantId == " + etudiantId0 + ")][0].moyenne").doesNotExist())
        // étudiantId1 : 1 exercice, moyenne=15.0
        .andExpect(jsonPath("$[?(@.etudiantId == " + etudiantId1 + ")][0].exercicesDeposes").value(1))
        .andExpect(jsonPath("$[?(@.etudiantId == " + etudiantId1 + ")][0].moyenne").value(15.0))
        // étudiantId2 : relecteur de 2 relectures, 1 EN_ATTENTE → relecturesEnAttente=1
        .andExpect(
            jsonPath("$[?(@.etudiantId == " + etudiantId2 + ")][0].relecturesEnAttente").value(1))
        // étudiantId3 : 1 exercice, moyenne=null
        .andExpect(jsonPath("$[?(@.etudiantId == " + etudiantId3 + ")][0].exercicesDeposes").value(1))
        .andExpect(jsonPath("$[?(@.etudiantId == " + etudiantId3 + ")][0].moyenne").doesNotExist());
  }
}
}
