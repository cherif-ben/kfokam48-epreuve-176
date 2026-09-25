package com.kfokam48.backend.service;

import com.kfokam48.backend.dto.RelectureAssigneeResponse;
import com.kfokam48.backend.dto.RelectureResponse;
import com.kfokam48.backend.dto.RendreRelectureRequest;
import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.Relecture;
import com.kfokam48.backend.entity.Session;
import com.kfokam48.backend.entity.StatutRelecture;
import com.kfokam48.backend.erreur.AutoRelectureException;
import com.kfokam48.backend.erreur.ExerciceInconnuException;
import com.kfokam48.backend.erreur.NoteInvalideException;
import com.kfokam48.backend.erreur.RelectureDejaRendueException;
import com.kfokam48.backend.erreur.RelectureInconnueException;
import com.kfokam48.backend.erreur.RelecteurNonAssigneException;
import com.kfokam48.backend.erreur.SessionInconnueException;
import com.kfokam48.backend.repository.ExerciceRepository;
import com.kfokam48.backend.repository.RelectureRepository;
import com.kfokam48.backend.repository.SessionRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EF9 / EF10 / RG11 (Q10) — rendre ou corriger une note (0-20) et un commentaire. L'exercice
 * passe a RELU a la premiere reddition. RG11 : la note est modifiable tant que la session n'est
 * pas cloturee (Q10 > Q15 : la note devient definitive a la cloture, cf. section 7).
 *
 * <p>Controle d'acces : l'appelant doit etre le relecteur assigne (RG2 interdit l'auto-relecture).
 * La note doit etre un entier 0-20 (RG3).
 */
@Service
public class RelectureService {

  private final RelectureRepository relectureRepository;
  private final ExerciceRepository exerciceRepository;
  private final SessionRepository sessionRepository;
  private final Clock horloge;

  public RelectureService(
      RelectureRepository relectureRepository,
      ExerciceRepository exerciceRepository,
      SessionRepository sessionRepository,
      Clock horloge) {
    this.relectureRepository = relectureRepository;
    this.exerciceRepository = exerciceRepository;
    this.sessionRepository = sessionRepository;
    this.horloge = horloge;
  }

  /**
   * EF9 / EF10 / RG11 (Q10) — le relecteur rend, puis corrige, sa note (0-20) et son commentaire.
   * L'exercice passe a RELU a la premiere reddition. RG11 : la correction est possible tant que
   * la session n'est pas cloturee (Q10 : la note devient definitive a la cloture, RG12).
   *
   * @param relectureId identifiant de la relecture (chemin).
   * @param relecteurId  identifiant de l'appelant (corps de la requete).
   * @param requete      note et commentaire.
   * @return la relecture mise a jour (sans le nom du relecteur, RG6).
   */
  @Transactional
  public RelectureResponse rendre(Long relectureId, Long relecteurId, RendreRelectureRequest requete) {
    Relecture relecture =
        relectureRepository.findById(relectureId).orElseThrow(RelectureInconnueException::new);

    // Verification que l'appelant est bien le relecteur assigne (403 si non).
    if (!relecteurId.equals(relecture.getRelecteurId())) {
      throw new RelecteurNonAssigneException();
    }

    Exercice exercice =
        exerciceRepository
            .findById(relecture.getExerciceId())
            .orElseThrow(ExerciceInconnuException::new);

    // RG2 (Q5) : un etudiant ne peut pas relire son propre exercice.
    if (relecteurId.equals(exercice.getEtudiantId())) {
      throw new AutoRelectureException();
    }

    // RG3 (Q9) : la note doit etre un entier entre 0 et 20.
    if (requete.note() == null || requete.note() < 0 || requete.note() > 20) {
      throw new NoteInvalideException();
    }

    // Q10 / RG11 / RG12 : la note est definitive des que la session est cloturee (409).
    Session session =
        sessionRepository
            .findById(exercice.getSessionId())
            .orElseThrow(SessionInconnueException::new);
    if (session.estCloturee()) {
      throw new RelectureDejaRendueException();
    }

    LocalDateTime maintenant = LocalDateTime.now(horloge);
    boolean premiereRendue = relecture.getStatut() != StatutRelecture.RENDUE;
    if (premiereRendue) {
      // EF9 : premiere reddition — la note est posee, l'exercice passe a RELU.
      relecture.rendre(requete.note(), requete.commentaire(), maintenant);
      exercice.marquerRelu(maintenant);
    } else {
      // EF10 / RG11 (Q10) : correction avant cloture — la note est remplacee, modifieeAt est
      // mis a jour, rendueAt (premiere reddition) et le statut RELU de l'exercice sont preserves.
      relecture.corriger(requete.note(), requete.commentaire(), maintenant);
    }

    relectureRepository.save(relecture);
    exerciceRepository.save(exercice);

    return RelectureResponse.depuis(relecture);
  }

  /**
   * EF8 / RG5 — liste des relectures assignées à un relecteur, filtrable par statut. Chaque ligne
   * porte le lien de l'exercice à relire. RG6 (Q8) : ni le nom de l'auteur ni celui du relecteur
   * ne sont exposés.
   */
  @Transactional(readOnly = true)
  public List<RelectureAssigneeResponse> listerAssignees(Long relecteurId, StatutRelecture statut) {
    return relectureRepository.findByRelecteurId(relecteurId).stream()
        .filter(r -> statut == null || r.getStatut() == statut)
        .map(
            r -> {
              String lien =
                  exerciceRepository.findById(r.getExerciceId()).map(Exercice::getLien).orElse(null);
              return RelectureAssigneeResponse.depuis(
                  r.getId(), r.getExerciceId(), lien, r.getStatut(), r.getNote(), r.getCommentaire());
            })
        .toList();
  }
}