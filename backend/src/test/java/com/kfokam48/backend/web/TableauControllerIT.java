package com.kfokam48.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.Presence;
import com.kfokam48.backend.entity.Relecture;
import com.kfokam48.backend.entity.Session;
import com.kfokam48.backend.entity.SourcePresence;
import com.kfokam48.backend.entity.StatutExercice;
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

/**
 * EF12 — intégration de {@code GET /api/tableau?promotionId=} : 200 (structure + agrégation) et
 * 404. Les étudiants utilisés (indices 50+) ne sont touchés par aucun autre test : les compteurs
 * exacts restent valides malgré le contexte H2 partagé.
 */
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

  private Long creerSession(String code) {
    Session session =
        new Session("IT tableau", 1L, 1L, code, LocalDateTime.now(), LocalDateTime.now().plusMinutes(30));
    return sessionRepository.save(session).getId();
  }

  @Test
  void tableau_promotionValide_retourne200AvecUneLigneParEtudiant() throws Exception {
    Long promotionId = promotionId();
    List<Etudiant> etudiants = etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId);

    mockMvc
        .perform(get("/api/tableau").param("promotionId", String.valueOf(promotionId)))
        .andExpect(status().isOk())
        // Une ligne par étudiant de la promotion (Q16).
        .andExpect(jsonPath("$", hasSize(etudiants.size())))
        .andExpect(jsonPath("$[0].etudiantId").exists())
        .andExpect(jsonPath("$[0].nom").exists())
        .andExpect(jsonPath("$[0].presences").exists())
        .andExpect(jsonPath("$[0].exercicesDeposes").exists())
        .andExpect(jsonPath("$[0].relecturesEnAttente").exists());
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
    List<Etudiant> etudiants = etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId);
    Long sessionId = creerSession("ITTAB1");

    // Indices 50+ : aucun autre test n'utilise ces étudiants (compteurs exacts garantis).
    Long etAuteur1 = etudiants.get(51).getId();
    Long etAuteur2 = etudiants.get(53).getId();
    Long etRelecteur = etudiants.get(52).getId();
    Long etPresent = etudiants.get(50).getId();

    // etudiant50 : 1 présence.
    presenceRepository.save(
        new Presence(sessionId, etPresent, SourcePresence.ETUDIANT, LocalDateTime.now()));

    // etudiant51 : 1 exercice déposé + relecture RENDUE (note 15) → moyenne 15.0.
    Exercice exercice1 =
        new Exercice(
            sessionId, etAuteur1, "https://exemple.test/1", StatutExercice.EN_ATTENTE_RELECTURE,
            LocalDateTime.now());
    Long exercice1Id = exerciceRepository.save(exercice1).getId();
    Relecture relecture1 = new Relecture(exercice1Id, etRelecteur, LocalDateTime.now());
    relecture1.rendre(15, "bien", LocalDateTime.now());
    relectureRepository.save(relecture1);

    // etudiant53 : 1 exercice déposé + relecture EN_ATTENTE assignée au même relecteur.
    Exercice exercice2 =
        new Exercice(
            sessionId, etAuteur2, "https://exemple.test/2", StatutExercice.EN_ATTENTE_RELECTURE,
            LocalDateTime.now());
    Long exercice2Id = exerciceRepository.save(exercice2).getId();
    relectureRepository.save(new Relecture(exercice2Id, etRelecteur, LocalDateTime.now()));

    mockMvc
        .perform(get("/api/tableau").param("promotionId", String.valueOf(promotionId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(etudiants.size()))
        // etudiant50 : 1 présence, aucun exercice, moyenne null (Q16 : « — », pas 0).
        .andExpect(jsonPath("$[?(@.etudiantId == " + etPresent + ")].presences").value(hasItem(1)))
        .andExpect(
            jsonPath("$[?(@.etudiantId == " + etPresent + ")].moyenne").value(hasItem(nullValue())))
        // etudiant51 : 1 exercice déposé, moyenne 15.0, 0 relecture en attente.
        .andExpect(
            jsonPath("$[?(@.etudiantId == " + etAuteur1 + ")].exercicesDeposes").value(hasItem(1)))
        .andExpect(jsonPath("$[?(@.etudiantId == " + etAuteur1 + ")].moyenne").value(hasItem(15.0)))
        .andExpect(
            jsonPath("$[?(@.etudiantId == " + etAuteur1 + ")].relecturesEnAttente")
                .value(hasItem(0)))
        // etudiant52 (relecteur) : 1 relecture EN_ATTENTE assignée (Q11).
        .andExpect(
            jsonPath("$[?(@.etudiantId == " + etRelecteur + ")].relecturesEnAttente")
                .value(hasItem(1)))
        // etudiant53 : 1 exercice, aucune note reçue → moyenne null.
        .andExpect(
            jsonPath("$[?(@.etudiantId == " + etAuteur2 + ")].exercicesDeposes").value(hasItem(1)))
        .andExpect(
            jsonPath("$[?(@.etudiantId == " + etAuteur2 + ")].moyenne").value(hasItem(nullValue())));

    // Les données de démo existent : 60 étudiants dans la promotion (ENF2 : volume réel).
    assertThat(etudiants).hasSize(60);
  }
}
