package com.kfokam48.backend.service;

import com.kfokam48.backend.dto.DeposerExerciceRequest;
import com.kfokam48.backend.dto.ExerciceCompletResponse;
import com.kfokam48.backend.dto.ExerciceResponse;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.Session;
import com.kfokam48.backend.entity.StatutExercice;
import com.kfokam48.backend.erreur.EtudiantInconnuException;
import com.kfokam48.backend.erreur.ExerciceDejaDeposeException;
import com.kfokam48.backend.erreur.ExerciceInconnuException;
import com.kfokam48.backend.erreur.LienInvalideException;
import com.kfokam48.backend.erreur.RelectureDejaCommenceeException;
import com.kfokam48.backend.erreur.SessionClotureeException;
import com.kfokam48.backend.erreur.SessionInconnueException;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.ExerciceRepository;
import com.kfokam48.backend.repository.SessionRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** EF6 / EF7 — dépôt et remplacement du lien d'un exercice. */
@Service
public class ExerciceService {

  private final ExerciceRepository exerciceRepository;
  private final SessionRepository sessionRepository;
  private final EtudiantRepository etudiantRepository;
  private final Clock horloge;

  public ExerciceService(
      ExerciceRepository exerciceRepository,
      SessionRepository sessionRepository,
      EtudiantRepository etudiantRepository,
      Clock horloge) {
    this.exerciceRepository = exerciceRepository;
    this.sessionRepository = sessionRepository;
    this.etudiantRepository = etudiantRepository;
    this.horloge = horloge;
  }

  /**
   * EF6 / RG9 / Q12 — dépose le lien d'un exercice. Le dépôt reste possible après
   * {@code expirationAt} de la session, mais pas après sa clôture (Q12).
   *
   * <p>Hypothèse §7 : aucun contrôle de présence préalable — un étudiant peut déposer même s'il
   * n'a pas marqué sa présence.
   */
  @Transactional
  public ExerciceResponse deposer(DeposerExerciceRequest requete) {
    String lien = validerLien(requete.lien());
    Session session =
        sessionRepository.findById(requete.sessionId()).orElseThrow(SessionInconnueException::new);

    // Q12 / RG14 : plus aucun dépôt dès que la session est clôturée.
    if (session.estCloturee()) {
      throw new SessionClotureeException();
    }

    Etudiant etudiant =
        etudiantRepository.findById(requete.etudiantId()).orElseThrow(EtudiantInconnuException::new);

    if (exerciceRepository.existsBySessionIdAndEtudiantId(session.getId(), etudiant.getId())) {
      throw new ExerciceDejaDeposeException();
    }

    LocalDateTime maintenant = LocalDateTime.now(horloge);
    Exercice exercice =
        new Exercice(
            session.getId(), etudiant.getId(), lien, StatutExercice.DEPOSE, maintenant);
    // Q11 : en sortie de dépôt, l'exercice attend sa relecture (assignation traitée par US-5).
    exercice.marquerEnAttenteDeRelecture(maintenant);

    return ExerciceResponse.depuis(exerciceRepository.save(exercice));
  }

  /** EF7 / RG10 (Q13) — remplace le lien tant que la relecture n'a pas commencé. */
  @Transactional
  public ExerciceResponse remplacerLien(Long exerciceId, String nouveauLien) {
    String lien = validerLien(nouveauLien);
    Exercice exercice =
        exerciceRepository.findById(exerciceId).orElseThrow(ExerciceInconnuException::new);
    Session session =
        sessionRepository
            .findById(exercice.getSessionId())
            .orElseThrow(SessionInconnueException::new);

    if (session.estCloturee() || exercice.getStatut() == StatutExercice.RELU) {
      throw new RelectureDejaCommenceeException();
    }

    exercice.remplacerLien(lien, LocalDateTime.now(horloge));
    return ExerciceResponse.depuis(exerciceRepository.save(exercice));
  }

  /** RG6 (Q8) — les exercices d'un étudiant, sans jamais exposer l'identité du relecteur. */
  @Transactional(readOnly = true)
  public List<ExerciceCompletResponse> listerPourEtudiant(Long etudiantId, Long sessionId) {
    List<Exercice> exercices =
        sessionId == null
            ? exerciceRepository.findByEtudiantIdOrderByCreatedAtDesc(etudiantId)
            : exerciceRepository.findByEtudiantIdAndSessionIdOrderByCreatedAtDesc(
                etudiantId, sessionId);
    return exercices.stream().map(ExerciceCompletResponse::depuis).toList();
  }

  /** Validation du lien côté API uniquement (F3 : aucune règle dupliquée côté front). */
  static String validerLien(String lien) {
    String candidat = lien == null ? "" : lien.trim();
    if (candidat.isEmpty()) {
      throw new LienInvalideException();
    }
    try {
      URI uri = new URI(candidat);
      boolean http = "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
      if (!http || uri.getHost() == null || uri.getHost().isBlank()) {
        throw new LienInvalideException();
      }
      return candidat;
    } catch (URISyntaxException erreur) {
      throw new LienInvalideException();
    }
  }
}
