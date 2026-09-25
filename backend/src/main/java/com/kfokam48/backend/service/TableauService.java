package com.kfokam48.backend.service;

import com.kfokam48.backend.dto.TableauLigneResponse;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Session;
import com.kfokam48.backend.erreur.PromotionInconnueException;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.ExerciceRepository;
import com.kfokam48.backend.repository.PresenceRepository;
import com.kfokam48.backend.repository.PromotionRepository;
import com.kfokam48.backend.repository.RelectureRepository;
import com.kfokam48.backend.repository.SessionRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EF12 — agrégation du tableau récapitulatif par étudiant (Q14, Q16).
 *
 * <p>Le formateur consulte, pour chaque étudiant de sa promotion : son nombre de présences, le
 * nombre d'exercices déposés, la moyenne de ses notes reçues (ou {@code null} s'il n'en a pas,
 * Q16) et le nombre de relectures qui lui sont assignées mais non rendues (Q11). La moyenne est
 * calculée côté API (F3) — le front ne la recalcule jamais.
 *
 * <p>Toutes les agrégations passent par 4 requêtes batch ({@code GROUP BY}) indépendantes du nombre
 * d'étudiants, ce qui garde le temps de réponse < 2 s pour 60 étudiants (ENF2).
 */
@Service
public class TableauService {

  private final PromotionRepository promotionRepository;
  private final EtudiantRepository etudiantRepository;
  private final SessionRepository sessionRepository;
  private final PresenceRepository presenceRepository;
  private final ExerciceRepository exerciceRepository;
  private final RelectureRepository relectureRepository;

  public TableauService(
      PromotionRepository promotionRepository,
      EtudiantRepository etudiantRepository,
      SessionRepository sessionRepository,
      PresenceRepository presenceRepository,
      ExerciceRepository exerciceRepository,
      RelectureRepository relectureRepository) {
    this.promotionRepository = promotionRepository;
    this.etudiantRepository = etudiantRepository;
    this.sessionRepository = sessionRepository;
    this.presenceRepository = presenceRepository;
    this.exerciceRepository = exerciceRepository;
    this.relectureRepository = relectureRepository;
  }

  @Transactional
  public List<TableauLigneResponse> tableau(Long promotionId) {
    if (!promotionRepository.existsById(promotionId)) {
      throw new PromotionInconnueException();
    }

    List<Etudiant> etudiants = etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId);
    List<Long> sessionIds =
        sessionRepository.findByPromotionIdOrderByOuvertureAtDesc(promotionId).stream()
            .map(Session::getId)
            .toList();

    Map<Long, Long> presences =
        sessionIds.isEmpty()
            ? Collections.emptyMap()
            : toMapLong(presenceRepository.countPresencesParEtudiant(sessionIds));
    Map<Long, Long> exercices =
        sessionIds.isEmpty()
            ? Collections.emptyMap()
            : toMapLong(exerciceRepository.countExercicesParEtudiant(sessionIds));
    Map<Long, Double> moyennes =
        sessionIds.isEmpty()
            ? Collections.emptyMap()
            : toMapDouble(relectureRepository.moyenneNoteParEtudiant(sessionIds));
    Map<Long, Long> relectures =
        sessionIds.isEmpty()
            ? Collections.emptyMap()
            : toMapLong(relectureRepository.countRelecturesEnAttenteParEtudiant(sessionIds));

    return etudiants.stream()
        .map(
            e ->
                TableauLigneResponse.depuis(
                    e,
                    presences.getOrDefault(e.getId(), 0L),
                    exercices.getOrDefault(e.getId(), 0L),
                    moyennes.get(e.getId()),
                    relectures.getOrDefault(e.getId(), 0L)))
        .toList();
  }

  private static Map<Long, Long> toMapLong(List<Object[]> rows) {
    return rows.stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
  }

  private static Map<Long, Double> toMapDouble(List<Object[]> rows) {
    return rows.stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Double) row[1]));
  }
}
