package com.kfokam48.backend.service;

import com.kfokam48.backend.dto.MarquerPresenceRequest;
import com.kfokam48.backend.dto.PresenceResponse;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Presence;
import com.kfokam48.backend.entity.Session;
import com.kfokam48.backend.entity.SourcePresence;
import com.kfokam48.backend.erreur.CodeExpireException;
import com.kfokam48.backend.erreur.CodeInconnuException;
import com.kfokam48.backend.erreur.DejaPresentException;
import com.kfokam48.backend.erreur.EtudiantInconnuException;
import com.kfokam48.backend.erreur.PresenceSessionClotureeException;
import com.kfokam48.backend.erreur.SessionInconnueException;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.PresenceRepository;
import com.kfokam48.backend.repository.SessionRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EF2 / EF3 / EF4 — marquage de présence avec un code.
 *
 * <p>Ordre de vérification imposé par le ticket : code inconnu (400) → code expiré ou session
 * clôturée (410) → étudiant inconnu (404) → déjà présent (409). L'unicité est aussi garantie par
 * la contrainte {@code UNIQUE(session_id, etudiant_id)} en base (RG12).
 */
@Service
public class PresenceService {

  private final PresenceRepository presenceRepository;
  private final SessionRepository sessionRepository;
  private final EtudiantRepository etudiantRepository;
  private final Clock horloge;

  public PresenceService(
      PresenceRepository presenceRepository,
      SessionRepository sessionRepository,
      EtudiantRepository etudiantRepository,
      Clock horloge) {
    this.presenceRepository = presenceRepository;
    this.sessionRepository = sessionRepository;
    this.etudiantRepository = etudiantRepository;
    this.horloge = horloge;
  }

  @Transactional
  public PresenceResponse marquer(MarquerPresenceRequest requete, SourcePresence source) {
    String code = requete.code().trim().toUpperCase();
    Session session = sessionRepository.findByCode(code).orElseThrow(CodeInconnuException::new);

    LocalDateTime maintenant = LocalDateTime.now(horloge);
    // RG1 (Q2) : le code expire 15 min après l'ouverture. RG14 (Q3) : rien n'est possible après clôture.
    if (session.estCloturee() || session.estExpiree(maintenant)) {
      throw new CodeExpireException();
    }

    Etudiant etudiant =
        etudiantRepository.findById(requete.etudiantId()).orElseThrow(EtudiantInconnuException::new);

    // RG12 : une seule présence par couple (session, étudiant).
    if (presenceRepository.existsBySessionIdAndEtudiantId(session.getId(), etudiant.getId())) {
      throw new DejaPresentException();
    }

    Presence presence =
        presenceRepository.save(
            new Presence(session.getId(), etudiant.getId(), source, maintenant));
    return PresenceResponse.depuis(presence);
  }

  /** EF5 / RG11 (Q14) — marquage par un étudiant ou par le formateur (source {@code FORMATEUR}). */
  @Transactional
  public PresenceResponse marquerParEtudiant(MarquerPresenceRequest requete) {
    SourcePresence source =
        requete.source() != null ? requete.source() : SourcePresence.ETUDIANT;
    return marquer(requete, source);
  }

  /**
   * EF5 / RG11 — le formateur ajoute manuellement la présence d'un étudiant sur une session
   * (source {@code FORMATEUR}, Q14). L'unicité (session, étudiant) s'applique quelle que soit la
   * source (RG12) et la session clôturée verrouille l'ajout (RG14).
   */
  @Transactional
  public PresenceResponse ajouterPresenceFormateur(Long sessionId, Long etudiantId) {
    Session session =
        sessionRepository.findById(sessionId).orElseThrow(SessionInconnueException::new);
    if (session.estCloturee()) {
      // Contrat §/api/sessions/{id}/presences : 410 SESSION_CLOTUREE après clôture (RG14).
      throw new PresenceSessionClotureeException();
    }
    Etudiant etudiant =
        etudiantRepository.findById(etudiantId).orElseThrow(EtudiantInconnuException::new);
    if (presenceRepository.existsBySessionIdAndEtudiantId(session.getId(), etudiant.getId())) {
      // RG12 : une seule présence par couple (session, étudiant), quelle que soit la source.
      throw new DejaPresentException();
    }
    Presence presence =
        presenceRepository.save(
            new Presence(session.getId(), etudiant.getId(), SourcePresence.FORMATEUR, LocalDateTime.now(horloge)));
    return PresenceResponse.depuis(presence);
  }
}
