package com.kfokam48.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.dto.OuvrirSessionRequest;
import com.kfokam48.backend.dto.SessionResponse;
import com.kfokam48.backend.entity.Formateur;
import com.kfokam48.backend.entity.Promotion;
import com.kfokam48.backend.entity.Session;
import com.kfokam48.backend.erreur.PromotionInconnueException;
import com.kfokam48.backend.repository.FormateurRepository;
import com.kfokam48.backend.repository.PromotionRepository;
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

/** EF1 / RG1 (Q2) — test unitaire de la règle « le code expire 15 minutes après l'ouverture ». */
@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

  private static final Instant INSTANT_FIGE = Instant.parse("2026-09-25T08:00:00Z");

  @Mock private SessionRepository sessionRepository;
  @Mock private PromotionRepository promotionRepository;
  @Mock private FormateurRepository formateurRepository;

  private SessionService sessionService;

  @BeforeEach
  void initialiser() {
    Clock horlogeFixe = Clock.fixed(INSTANT_FIGE, ZoneOffset.UTC);
    sessionService =
        new SessionService(sessionRepository, promotionRepository, formateurRepository, horlogeFixe);
  }

  @Test
  void ouvrir_genereUnCodeDeSixCaracteresEtExpireQuinzeMinutesApresLOuverture() {
    when(promotionRepository.findById(1L)).thenReturn(Optional.of(new Promotion("KFOKAM48")));
    when(formateurRepository.findFirstByOrderByIdAsc())
        .thenReturn(Optional.of(new Formateur("Faroukou Cherif Ben")));
    when(sessionRepository.existsByCode(any())).thenReturn(false);
    when(sessionRepository.save(any(Session.class))).thenAnswer(appel -> appel.getArgument(0));

    SessionResponse reponse = sessionService.ouvrir(new OuvrirSessionRequest("Cours du jour", 1L));

    assertThat(reponse.code()).hasSize(6);
    assertThat(reponse.ouvertureAt()).isEqualTo(LocalDateTime.ofInstant(INSTANT_FIGE, ZoneOffset.UTC));
    assertThat(reponse.expirationAt()).isEqualTo(reponse.ouvertureAt().plusMinutes(15));
  }

  @Test
  void ouvrir_avecPromotionInconnue_levePromotionInconnue() {
    when(promotionRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> sessionService.ouvrir(new OuvrirSessionRequest("Cours", 99L)))
        .isInstanceOf(PromotionInconnueException.class);
  }
}
