package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.Relecture;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Accès aux relectures (B3). */
public interface RelectureRepository extends JpaRepository<Relecture, Long> {

  /** RG4 (Q6) : un seul relecteur par exercice — recherche par clé naturelle. */
  Optional<Relecture> findByExerciceId(Long exerciceId);

  boolean existsByExerciceId(Long exerciceId);

  /** EF12 (Q16) — moyenne des notes rendues par étudiant, pour les exercices d'une promotion. */
  @Query(
      "SELECT ex.etudiantId, AVG(r.note) "
          + "FROM Relecture r JOIN Exercice ex ON r.exerciceId = ex.id "
          + "WHERE ex.sessionId IN :sessionIds AND r.statut = 'RENDUE' "
          + "GROUP BY ex.etudiantId")
  List<Object[]> moyenneNoteParEtudiant(@Param("sessionIds") List<Long> sessionIds);

  /** EF12 (Q11) — relectures en attente non rendues par relecteur, pour les exercices d'une promotion. */
  @Query(
      "SELECT r.relecteurId, COUNT(r) "
          + "FROM Relecture r JOIN Exercice e ON r.exerciceId = e.id "
          + "WHERE e.sessionId IN :sessionIds AND r.statut = 'EN_ATTENTE' "
          + "GROUP BY r.relecteurId")
  List<Object[]> countRelecturesEnAttenteParEtudiant(@Param("sessionIds") List<Long> sessionIds);
}

