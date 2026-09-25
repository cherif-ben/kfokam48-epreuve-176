package com.kfokam48.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.dto.DeposerExerciceRequest;
import com.kfokam48.backend.dto.ExerciceResponse;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.Session;
import com.kfokam48.backend.entity.StatutExercice;
import com.kfokam48.backend.erreur.ExerciceDejaDeposeException;
import com.kfokam48.backend.erreur.LienInvalideException;
import com.kfokam48.backend.erreur.RelectureDejaCommenceeException;
import com.kfokam48.backend.erreur.SessionClotureeException;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.ExerciceRepository;
import com.kfokam48.backend.repository.SessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** EF6 / EF7 — tests unitaires : validation du lien, RG9 (unicité), Q12 (dépôt jusqu'à clôture), RG10. */
@ExtendWith(MockitoExtension.class)
class ExerciceServiceTest {

  private static final Instant INSTANT_FIGE = Instant.parse("2026-09-25T08:00:00Z");
  private static final LocalDateTime MAINTENANT =
      LocalDateTime.ofInstant(INSTANT_FIGE, ZoneOffset.UTC);

  @Mock private ExerciceRepository exerciceRepository;
  @Mock private SessionRepository sessionRepository;
  @Mock private EtudiantRepository etudiantRepository;

  private ExerciceService exerciceService;

  @BeforeEach
  void initialiser() {
    exerciceService =
        new ExerciceService(
            exerciceRepository, sessionRepository, etudiantRepository, Clock.fixed(INSTANT_FIGE, ZoneOffset.UTC));
  }

  private Session sessionExpireeMaisNonCloturee() {
    return new Session(
        "Cours", 1L, 1L, "ABC234", MAINTENANT.minusMinutes(30), MAINTENANT.minusMinutes(15));
  }

  private Etudiant etudiantAvecId(Long id) {
    Etudiant etudiant = mock(Etudiant.class);
    when(etudiant.getId()).thenReturn(id);
    return etudiant;
  }

  @Test
  void deposer_apresExpirationMaisAvantCloture_accepteEtPasseEnAttenteDeRelecture() {
    // Q12 : le dépôt reste possible jusqu'à la clôture, même après expirationAt.
    Etudiant etudiant = etudiantAvecId(2L);
    when(sessionRepository.findById(1L)).thenReturn(Optional.of(sessionExpireeMaisNonCloturee()));
    when(etudiantRepository.findById(2L)).thenReturn(Optional.of(etudiant));
    when(exerciceRepository.existsBySessionIdAndEtudiantId(any(), any())).thenReturn(false);
    when(exerciceRepository.save(any(Exercice.class))).thenAnswer(appel -> appel.getArgument(0));

    ExerciceResponse reponse =
        exerciceService.deposer(
            new DeposerExerciceRequest(1L, 2L, "https://exemple.test/mon-exercice"));

    assertThat(reponse.statut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);
  }

  @Test
  void deposer_avecLienMalForme_leveLienInvalide() {
    assertThatThrownBy(
            () -> exerciceService.deposer(new DeposerExerciceRequest(1L, 2L, "pas une url")))
        .isInstanceOf(LienInvalideException.class);
  }

  @Test
  void deposer_deuxFoisPourLaMemeSession_leveExerciceDejaDepose() {
    // RG9 : un seul exercice par couple (session, étudiant).
    Etudiant etudiant = etudiantAvecId(2L);
    when(sessionRepository.findById(1L)).thenReturn(Optional.of(sessionExpireeMaisNonCloturee()));
    when(etudiantRepository.findById(2L)).thenReturn(Optional.of(etudiant));
    when(exerciceRepository.existsBySessionIdAndEtudiantId(any(), any())).thenReturn(true);

    assertThatThrownBy(
            () ->
                exerciceService.deposer(
                    new DeposerExerciceRequest(1L, 2L, "https://exemple.test/mon-exercice")))
        .isInstanceOf(ExerciceDejaDeposeException.class);
  }

  @Test
  void deposer_apresCloture_leveSessionCloturee() {
    Session cloturee = sessionExpireeMaisNonCloturee();
    cloturee.cloturer(MAINTENANT);
    when(sessionRepository.findById(1L)).thenReturn(Optional.of(cloturee));

    assertThatThrownBy(
            () ->
                exerciceService.deposer(
                    new DeposerExerciceRequest(1L, 2L, "https://exemple.test/mon-exercice")))
        .isInstanceOf(SessionClotureeException.class);
  }

  @Test
  void remplacerLien_quandExerciceDejaRelu_leveRelectureDejaCommencee() {
    // RG10 (Q13) : plus de modification dès que la relecture a commencé.
    Exercice relu =
        new Exercice(1L, 2L, "https://exemple.test/v1", StatutExercice.RELU, MAINTENANT);
    when(exerciceRepository.findById(9L)).thenReturn(Optional.of(relu));
    when(sessionRepository.findById(1L)).thenReturn(Optional.of(sessionExpireeMaisNonCloturee()));

    assertThatThrownBy(() -> exerciceService.remplacerLien(9L, "https://exemple.test/v2"))
        .isInstanceOf(RelectureDejaCommenceeException.class);
  }
}
