package com.kfokam48.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
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
        .andExpect(jsonPath("$.length()").value(etudiants.size()))
        .andExpect(jsonPath("$[0].etudiantId").exists())
        .andExpect(jsonPath("$[0].nom").exists())
        // Le contexte H2 est partagé entre classes de tests : des données résiduelles
        // peuvent exister, on valide donc le type et la non-négativité des compteurs.
        .andExpect(jsonPath("$[0].presences").value(greaterThanOrEqualTo(0)))
        .andExpect(jsonPath("$[0].exercicesDeposes").value(greaterThanOrEqualTo(0)))
        .andExpect(jsonPath("$[0].relecturesEnAttente").value(greaterThanOrEqualTo(0)))
        // Q16 : la majorité des étudiants n'ont aucune note → moyenne null (affichée « — »).
        .andExpect(jsonPath("$[?(@.moyenne == null)]", hasSize(greaterThanOrEqualTo(55))));
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
  void tableau_avecDonnees_aggregueCorrectement() throws Exception {
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

    String responseBody =
        mockMvc
            .perform(get("/api/tableau").param("promotionId", String.valueOf(promotionId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(60))
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(responseBody).contains("\"etudiantId\":" + etudiantId0);
    assertThat(responseBody).contains("\"etudiantId\":" + etudiantId1);
    assertThat(responseBody).contains("\"etudiantId\":" + etudiantId2);
    assertThat(responseBody).contains("\"etudiantId\":" + etudiantId3);

    assertThat(responseBody).contains("\"presences\":1");
    assertThat(responseBody).contains("\"moyenne\":15.0");
    assertThat(responseBody).contains("\"relecturesEnAttente\":1");
  }
}
