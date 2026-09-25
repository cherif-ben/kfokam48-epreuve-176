package com.kfokam48.backend.service;

import com.kfokam48.backend.dto.DeposerExerciceRequest;
import com.kfokam48.backend.dto.ExerciceCompletResponse;
import com.kfokam48.backend.dto.ExerciceResponse;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.Presence;
import com.kfokam48.backend.entity.Relecture;
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
import com.kfokam48.backend.repository.PresenceRepository;
import com.kfokam48.backend.repository.RelectureRepository;
import com.kfokam48.backend.repository.SessionRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EF6 / EF7 / EF8 — dépôt, remplacement du lien et assignation automatique d'un relecteur.
 *
 * <p>RG2 (Q5) : un étudiant ne peut pas relire son propre exercice. RG4 (Q6) : un seul relecteur
 * par exercice (contrainte UNIQUE(exercice_id) en base). RG5 (Q7) : le relecteur est tiré au hasard
 * parmi les étudiants présents à la session. Q11 : si aucun relecteur n'est disponible, l'exercice
 * reste EN_ATTENTE_RELECTURE, sans erreur.
 */
@Service
public class ExerciceService {

  private final ExerciceRepository exerciceRepository;
  private final SessionRepository sessionRepository;
  private final EtudiantRepository etudiantRepository;
  private final PresenceRepository presenceRepository;
  private final RelectureRepository relectureRepository;
  private final Random aleatoire;
  private final Clock horloge;

  public ExerciceService(
      ExerciceRepository exerciceRepository,
      SessionRepository sessionRepository,
      EtudiantRepository etudiantRepository,
      PresenceRepository presenceRepository,
      RelectureRepository relectureRepository,
      Random aleatoire,
      Clock horloge) {
    this.exerciceRepository = exerciceRepository;
    this.sessionRepository = sessionRepository;
    this.etudiantRepository = etudiantRepository;
    this.presenceRepository = presenceRepository;
    this.relectureRepository = relectureRepository;
    this.aleatoire = aleatoire;
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
    exercice = exerciceRepository.save(exercice);

    // EF8 / RG2 / RG4 / RG5 (Q5, Q6, Q7) : assignation d'un relecteur parmi les présents.
    assignerRelecteur(exercice, maintenant);

    return ExerciceResponse.depuis(exercice);
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

  /**
   * EF8 / RG2 / RG4 / RG5 (Q5, Q6, Q7) — tire un relecteur au hasard parmi les étudiants présents
   * à la session de l'exercice, en excluant l'auteur. Si aucun relecteur n'est disponible (Q11),
   * l'exercice reste en attente et aucune relecture n'est créée.
   *
   * <p>La contrainte {@code UNIQUE(exercice_id)} de V1 garantit RG4 au niveau base : une seconde
   * relecture pour le même exercice déclencherait une violation d'intégrité (409
   * CONFLIT_DONNEES via le {@code GestionnaireErreurs}), pas un 500.
   */
  private void assignerRelecteur(Exercice exercice, LocalDateTime horodatage) {
    List<Presence> presents = presenceRepository.findBySessionId(exercice.getSessionId());
    Long auteurId = exercice.getEtudiantId();

    List<Long> candidats =
        presents.stream()
            .map(Presence::getEtudiantId)
            .filter(etudiantId -> !etudiantId.equals(auteurId))
            .toList();

    if (candidats.isEmpty()) {
      // Q11 : aucun autre étudiant présent → l'exercice reste EN_ATTENTE_RELECTURE, pas d'erreur.
      return;
    }

    Long relecteurId = candidats.get(aleatoire.nextInt(candidats.size()));
    relectureRepository.save(new Relecture(exercice.getId(), relecteurId, horodatage));
  }
}
