package com.kfokam48.backend.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kfokam48.backend.erreur.CodeExpireException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * B4 — preuve que toutes les erreurs sortent au format {@code { code, message }}, sans stack trace.
 * Un contrôleur de test local produit chaque famille d'erreur.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(GestionnaireErreursIT.ControleurDeTest.class)
class GestionnaireErreursIT {

  @Autowired private MockMvc mockMvc;

  @RestController
  static class ControleurDeTest {

    record CorpsDeTest(@NotBlank(message = "ne doit pas être vide") String obligatoire) {}

    @GetMapping("/api/test/erreur-metier")
    void erreurMetier() {
      throw new CodeExpireException();
    }

    @PostMapping("/api/test/champ-manquant")
    void champManquant(@Valid @RequestBody CorpsDeTest corps) {
      // le corps n'est jamais atteint : la validation échoue avant
    }

    @GetMapping("/api/test/erreur-interne")
    void erreurInterne() {
      throw new IllegalStateException("détail technique à ne jamais exposer");
    }
  }

  @Test
  void erreurMetier_ressortAvecSonCodeEtSonStatut() throws Exception {
    mockMvc
        .perform(get("/api/test/erreur-metier"))
        .andExpect(status().isGone())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code").value("CODE_EXPIRE"))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  void validation_ressortEn400ChampManquant() throws Exception {
    mockMvc
        .perform(
            post("/api/test/champ-manquant")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"obligatoire\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CHAMP_MANQUANT"))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  void corpsIllisible_ressortEn400CorpsInvalide() throws Exception {
    mockMvc
        .perform(
            post("/api/test/champ-manquant")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{pas-du-json"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CORPS_INVALIDE"));
  }

  @Test
  void ressourceInconnue_ressortEn404AuFormatDuContrat() throws Exception {
    mockMvc
        .perform(get("/api/route-qui-nexiste-pas"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("RESSOURCE_INCONNUE"))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  void erreurInattendue_ressortEn500SansStackTrace() throws Exception {
    mockMvc
        .perform(get("/api/test/erreur-interne"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value("ERREUR_INTERNE"))
        .andExpect(jsonPath("$.message").value("Une erreur interne est survenue. Réessayez dans un instant."))
        .andExpect(content().string(not(containsString("IllegalStateException"))))
        .andExpect(content().string(not(containsString("détail technique"))));
  }
}
