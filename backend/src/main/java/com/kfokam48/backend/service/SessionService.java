package com.kfokam48.backend.service;

import com.kfokam48.backend.dto.OuvrirSessionRequest;
import com.kfokam48.backend.dto.SessionDetailResponse;
import com.kfokam48.backend.dto.SessionResponse;
import com.kfokam48.backend.entity.Formateur;
import com.kfokam48.backend.entity.Promotion;
import com.kfokam48.backend.entity.Session;
import com.kfokam48.backend.erreur.FormateurInconnuException;
import com.kfokam48.backend.erreur.PromotionInconnueException;
import com.kfokam48.backend.erreur.SessionInconnueException;
import com.kfokam48.backend.repository.FormateurRepository;
import com.kfokam48.backend.repository.PromotionRepository;
import com.kfokam48.backend.repository.SessionRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EF1 — ouverture d'une session et génération du code de présence.
 *
 * <p>Le statut « OUVERTE » n'est pas une colonne : il se déduit de {@code clotureAt} (nul) et de
 * {@code expirationAt} (RG1, RG14), conformément au diagramme D2.
 */
@Service
public class SessionService {

  /** Alphabet sans caractères ambigus (0/O, 1/I/L) : le code est dicté à l'oral. */
  private static final String ALPHABET_CODE = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

  private static final int LONGUEUR_CODE = 6;
  private static final int TENTATIVES_GENERATION_CODE = 10;

  private final SessionRepository sessionRepository;
  private final PromotionRepository promotionRepository;
  private final FormateurRepository formateurRepository;
  private final Clock horloge;
  private final SecureRandom aleatoire = new SecureRandom();

  public SessionService(
      SessionRepository sessionRepository,
      PromotionRepository promotionRepository,
      FormateurRepository formateurRepository,
      Clock horloge) {
    this.sessionRepository = sessionRepository;
    this.promotionRepository = promotionRepository;
    this.formateurRepository = formateurRepository;
    this.horloge = horloge;
  }

  /**
   * EF1 / RG1 (Q2) — ouvre une session : code unique de 6 caractères et
   * {@code expirationAt = ouvertureAt + 15 min}.
   */
  @Transactional
  public SessionResponse ouvrir(OuvrirSessionRequest requete) {
    Promotion promotion =
        promotionRepository
            .findById(requete.promotionId())
            .orElseThrow(PromotionInconnueException::new);
    Formateur formateur =
        formateurRepository.findFirstByOrderByIdAsc().orElseThrow(FormateurInconnuException::new);

    LocalDateTime ouvertureAt = LocalDateTime.now(horloge);
    Session session =
        new Session(
            requete.titre().trim(),
            promotion.getId(),
            formateur.getId(),
            genererCodeUnique(),
            ouvertureAt,
            ouvertureAt.plus(Session.DUREE_VALIDITE_CODE));

    Session enregistree = sessionRepository.save(session);
    return new SessionResponse(
        enregistree.getId(),
        enregistree.getCode(),
        enregistree.getOuvertureAt(),
        enregistree.getExpirationAt());
  }

  @Transactional(readOnly = true)
  public SessionDetailResponse trouver(Long id) {
    return sessionRepository
        .findById(id)
        .map(SessionDetailResponse::depuis)
        .orElseThrow(SessionInconnueException::new);
  }

  /** Liste des sessions, filtrable par promotion et/ou formateur (écrans formateur et étudiant). */
  @Transactional(readOnly = true)
  public List<SessionDetailResponse> lister(Long promotionId, Long formateurId) {
    List<Session> sessions;
    if (promotionId != null && formateurId != null) {
      sessions =
          sessionRepository.findByPromotionIdAndFormateurIdOrderByOuvertureAtDesc(
              promotionId, formateurId);
    } else if (promotionId != null) {
      sessions = sessionRepository.findByPromotionIdOrderByOuvertureAtDesc(promotionId);
    } else if (formateurId != null) {
      sessions = sessionRepository.findByFormateurIdOrderByOuvertureAtDesc(formateurId);
    } else {
      sessions = sessionRepository.findAllByOrderByOuvertureAtDesc();
    }
    return sessions.stream().map(SessionDetailResponse::depuis).toList();
  }

  private String genererCodeUnique() {
    for (int tentative = 0; tentative < TENTATIVES_GENERATION_CODE; tentative++) {
      String code = codeAleatoire();
      if (!sessionRepository.existsByCode(code)) {
        return code;
      }
    }
    throw new IllegalStateException("Impossible de générer un code de présence unique.");
  }

  private String codeAleatoire() {
    StringBuilder code = new StringBuilder(LONGUEUR_CODE);
    for (int i = 0; i < LONGUEUR_CODE; i++) {
      code.append(ALPHABET_CODE.charAt(aleatoire.nextInt(ALPHABET_CODE.length())));
    }
    return code.toString();
  }
}
