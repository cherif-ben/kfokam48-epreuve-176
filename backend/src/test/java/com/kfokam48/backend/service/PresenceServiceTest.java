package com.kfokam48.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.dto.MarquerPresenceRequest;
import com.kfokam48.backend.dto.PresenceResponse;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Presence;
import com.kfokam48.backend.entity.Session;
import com.kfokam48.backend.entity.SourcePresence;
import com.kfokam48.backend.erreur.CodeExpireException;
import com.kfokam48.backend.erreur.CodeInconnuException;
import com.kfokam48.backend.erreur.DejaPresentException;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.PresenceRepository;
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

/** EF2 / EF3 / EF4 — tests unitaires des règles RG1 (expiration) et RG12 (unicité). */
@ExtendWith(MockitoExtension.class)
class PresenceServiceTest {

  private static final Instant INSTANT_FIGE = Instant.parse("2026-09-25T08:00:00Z");
  private static final LocalDateTime MAINTENANT =
      LocalDateTime.ofInstant(INSTANT_FIGE, ZoneOffset.UTC);

  @Mock private PresenceRepository presenceRepository;
  @Mock private SessionRepository sessionRepository;
  @Mock private EtudiantRepository etudiantRepository;

  private PresenceService presenceService;

  @BeforeEach
  void initialiser() {
    presenceService =
        new PresenceService(
            presenceRepository,
            sessionRepository,
            etudiantRepository,
            Clock.fixed(INSTANT_FIGE, ZoneOffset.UTC));
  }

  private static Session session(String code, LocalDateTime expirationAt) {
    return new Session("Cours", 1L, 1L, code, MAINTENANT.minusMinutes(1), expirationAt);
  }

  @Test
  void marquer_avecCodeValide_enregistreUnePresenceEtudiant() {
    Etudiant etudiant = mock(Etudiant.class);
    when(etudiant.getId()).thenReturn(1L);
    when(sessionRepository.findByCode("ABC234"))
        .thenReturn(Optional.of(session("ABC234", MAINTENANT.plusMinutes(14))));
    when(etudiantRepository.findById(1L)).thenReturn(Optional.of(etudiant));
    when(presenceRepository.existsBySessionIdAndEtudiantId(any(), any())).thenReturn(false);
    when(presenceRepository.save(any(Presence.class))).thenAnswer(appel -> appel.getArgument(0));

    PresenceResponse reponse =
        presenceService.marquerParEtudiant(new MarquerPresenceRequest("abc234", 1L));

    assertThat(reponse.source()).isEqualTo(SourcePresence.ETUDIANT);
    assertThat(reponse.etudiantId()).isEqualTo(1L);
  }

  @Test
  void marquer_avecSourceFormateur_enregistreUnePresenceFormateur() {
    Etudiant etudiant = mock(Etudiant.class);
    when(etudiant.getId()).thenReturn(1L);
    when(sessionRepository.findByCode("ABC234"))
        .thenReturn(Optional.of(session("ABC234", MAINTENANT.plusMinutes(14))));
    when(etudiantRepository.findById(1L)).thenReturn(Optional.of(etudiant));
    when(presenceRepository.existsBySessionIdAndEtudiantId(any(), any())).thenReturn(false);
    when(presenceRepository.save(any(Presence.class))).thenAnswer(appel -> appel.getArgument(0));

    PresenceResponse reponse =
        presenceService.marquerParEtudiant(
            new MarquerPresenceRequest("abc234", 1L, SourcePresence.FORMATEUR));

    assertThat(reponse.source()).isEqualTo(SourcePresence.FORMATEUR);
  }

  @Test
  void marquer_avecCodeExpire_leveCodeExpire() {
    // RG1 (Q2) : le code n'est valide que 15 minutes.
    when(sessionRepository.findByCode("ABC234"))
        .thenReturn(Optional.of(session("ABC234", MAINTENANT.minusMinutes(1))));

    assertThatThrownBy(() -> presenceService.marquerParEtudiant(new MarquerPresenceRequest("ABC234", 1L)))
        .isInstanceOf(CodeExpireException.class);
  }

  @Test
  void marquer_avecSessionCloturee_leveCodeExpire() {
    // RG14 (Q3) : pas de présence après clôture, même si le code n'a pas expiré.
    Session cloturee = session("ABC234", MAINTENANT.plusMinutes(14));
    cloturee.cloturer(MAINTENANT);
    when(sessionRepository.findByCode("ABC234")).thenReturn(Optional.of(cloturee));

    assertThatThrownBy(() -> presenceService.marquerParEtudiant(new MarquerPresenceRequest("ABC234", 1L)))
        .isInstanceOf(CodeExpireException.class);
  }

  @Test
  void marquer_avecCodeInconnu_leveCodeInconnu() {
    when(sessionRepository.findByCode("ZZZ999")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> presenceService.marquerParEtudiant(new MarquerPresenceRequest("ZZZ999", 1L)))
        .isInstanceOf(CodeInconnuException.class);
  }

  @Test
  void marquer_quandLaPresenceExisteDeja_leveDejaPresent() {
    // RG12 : une seule présence par couple (session, étudiant).
    when(sessionRepository.findByCode("ABC234")).thenReturn(Optional.of(session("ABC234", MAINTENANT.plusMinutes(14))));
    when(etudiantRepository.findById(1L)).thenReturn(Optional.of(new Etudiant("Etudiant 1", 1L)));
    when(presenceRepository.existsBySessionIdAndEtudiantId(any(), any())).thenReturn(true);

    assertThatThrownBy(() -> presenceService.marquerParEtudiant(new MarquerPresenceRequest("ABC234", 1L)))
        .isInstanceOf(DejaPresentException.class);
  }
}
