package com.kfokam48.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.dto.DeposerExerciceRequest;
import com.kfokam48.backend.dto.ExerciceResponse;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.Presence;
import com.kfokam48.backend.entity.Relecture;
import com.kfokam48.backend.entity.Session;
import com.kfokam48.backend.entity.SourcePresence;
import com.kfokam48.backend.entity.StatutExercice;
import com.kfokam48.backend.entity.StatutRelecture;
import com.kfokam48.backend.erreur.ExerciceDejaDeposeException;
import com.kfokam48.backend.erreur.LienInvalideException;
import com.kfokam48.backend.erreur.RelectureDejaCommenceeException;
import com.kfokam48.backend.erreur.SessionClotureeException;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.ExerciceRepository;
import com.kfokam48.backend.repository.PresenceRepository;
import com.kfokam48.backend.repository.RelectureRepository;
import com.kfokam48.backend.repository.SessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** EF6 / EF7 / EF8 — tests unitaires : validation du lien, RG9 (unicité), Q12, RG10, assignation (RG2/4/5). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExerciceServiceTest {

  private static final Instant INSTANT_FIGE = Instant.parse("2026-09-25T08:00:00Z");
  private static final LocalDateTime MAINTENANT =
      LocalDateTime.ofInstant(INSTANT_FIGE, ZoneOffset.UTC);

  @Mock private ExerciceRepository exerciceRepository;
  @Mock private SessionRepository sessionRepository;
  @Mock private EtudiantRepository etudiantRepository;
  @Mock private PresenceRepository presenceRepository;
  @Mock private RelectureRepository relectureRepository;

  private ExerciceService exerciceService;

  @BeforeEach
  void initialiser() {
    exerciceService =
        new ExerciceService(
            exerciceRepository,
            sessionRepository,
            etudiantRepository,
            presenceRepository,
            relectureRepository,
            new Random(0),
            Clock.fixed(INSTANT_FIGE, ZoneOffset.UTC));
  }

  private Session sessionExpireeMaisNonCloturee() {
    return new Session(
        "Cours", 1L, 1L, "ABC234", MAINTENANT.minusMinutes(30), MAINTENANT.minusMinutes(15));
  }

  /** Force l'id d'une entité non persistée (convention des tests unitaires, cf. note journal #16). */
  private static void forcerId(Object entite, Long id) {
    try {
      java.lang.reflect.Field idField = entite.getClass().getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(entite, id);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private Etudiant etudiantAvecId(Long id) {
    Etudiant etudiant = mock(Etudiant.class);
    when(etudiant.getId()).thenReturn(id);
    return etudiant;
  }

  @Test
  void deposer_apresExpirationMaisAvantCloture_accepteEtPasseEnAttenteDeRelecture() {
    // Q12 : le dépôt reste possible jusqu'à la clôture, même après expirationAt.
    Session session = sessionExpireeMaisNonCloturee();
    forcerId(session, 1L);
    Etudiant etudiant = etudiantAvecId(2L);
    when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
    when(etudiantRepository.findById(2L)).thenReturn(Optional.of(etudiant));
    when(exerciceRepository.existsBySessionIdAndEtudiantId(any(), any())).thenReturn(false);
    when(exerciceRepository.save(any(Exercice.class)))
        .thenAnswer(
            appel -> {
              Exercice ex = appel.getArgument(0);
              forcerId(ex, 42L);
              return ex;
            });
    when(presenceRepository.findBySessionId(1L)).thenReturn(List.of());
    when(relectureRepository.save(any(Relecture.class))).thenAnswer(appel -> appel.getArgument(0));

    ExerciceResponse reponse =
        exerciceService.deposer(
            new DeposerExerciceRequest(1L, 2L, "https://exemple.test/mon-exercice"));

    assertThat(reponse.statut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);
    // Q11 : aucun autre présent → aucune relecture créée, l'exercice reste en attente.
    verify(relectureRepository, times(0)).save(any());
  }

  @Test
  void deposer_assigneUnRelecteurDifferentDeLAuteur() {
    // EF8 / RG2 / RG5 (Q5, Q7) : le relecteur est tiré au hasard parmi les présents, jamais l'auteur.
    Session session = sessionExpireeMaisNonCloturee();
    forcerId(session, 1L);
    Etudiant auteur = etudiantAvecId(2L);
    when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
    when(etudiantRepository.findById(2L)).thenReturn(Optional.of(auteur));
    when(exerciceRepository.existsBySessionIdAndEtudiantId(any(), any())).thenReturn(false);
    when(exerciceRepository.save(any(Exercice.class)))
        .thenAnswer(
            appel -> {
              Exercice ex = appel.getArgument(0);
              forcerId(ex, 42L);
              return ex;
            });
    // Deux autres étudiants présents (3 et 4) ; l'auteur (2) est exclu par RG2.
    when(presenceRepository.findBySessionId(1L))
        .thenReturn(
            List.of(
                new Presence(1L, 2L, SourcePresence.ETUDIANT, MAINTENANT),
                new Presence(1L, 3L, SourcePresence.ETUDIANT, MAINTENANT),
                new Presence(1L, 4L, SourcePresence.ETUDIANT, MAINTENANT)));
    when(relectureRepository.save(any(Relecture.class))).thenAnswer(appel -> appel.getArgument(0));

    ExerciceResponse reponse =
        exerciceService.deposer(
            new DeposerExerciceRequest(1L, 2L, "https://exemple.test/mon-exercice"));

    assertThat(reponse.statut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);
    verify(relectureRepository, times(1)).save(any());
    verify(relectureRepository)
        .save(
            argThat(
                relecture ->
                    relecture.getExerciceId() == 42L
                        && relecture.getRelecteurId() != 2L
                        && (relecture.getRelecteurId() == 3L || relecture.getRelecteurId() == 4L)
                        && relecture.getStatut() == StatutRelecture.EN_ATTENTE));
  }

  @Test
  void deposer_sansAutrePresent_resteEnAttenteSansRelecture() {
    // Q11 : aucun relecteur éligible → l'exercice reste EN_ATTENTE_RELECTURE, pas d'erreur 500.
    Session session = sessionExpireeMaisNonCloturee();
    forcerId(session, 1L);
    Etudiant auteur = etudiantAvecId(2L);
    when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
    when(etudiantRepository.findById(2L)).thenReturn(Optional.of(auteur));
    when(exerciceRepository.existsBySessionIdAndEtudiantId(any(), any())).thenReturn(false);
    when(exerciceRepository.save(any(Exercice.class)))
        .thenAnswer(
            appel -> {
              Exercice ex = appel.getArgument(0);
              forcerId(ex, 43L);
              return ex;
            });
    // Seul l'auteur est présent → aucun candidat éligible (RG2).
    when(presenceRepository.findBySessionId(1L))
        .thenReturn(
            List.of(new Presence(1L, 2L, SourcePresence.ETUDIANT, MAINTENANT)));

    ExerciceResponse reponse =
        exerciceService.deposer(
            new DeposerExerciceRequest(1L, 2L, "https://exemple.test/mon-exercice"));

    assertThat(reponse.statut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);
    verify(relectureRepository, times(0)).save(any());
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
