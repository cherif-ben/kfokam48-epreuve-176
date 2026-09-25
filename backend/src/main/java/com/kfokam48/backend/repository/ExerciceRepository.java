package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.Exercice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Accès aux exercices (B3). */
public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

  boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

  Optional<Exercice> findBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

  List<Exercice> findByEtudiantIdOrderByCreatedAtDesc(Long etudiantId);

  List<Exercice> findByEtudiantIdAndSessionIdOrderByCreatedAtDesc(Long etudiantId, Long sessionId);

  List<Exercice> findBySessionId(Long sessionId);

  long countByEtudiantId(Long etudiantId);

  /** EF12 (Q16) — comptage d'exercices déposés par étudiant, pour les sessions d'une promotion. */
  @Query(
      "SELECT e.etudiantId, COUNT(e) FROM Exercice e WHERE e.sessionId IN :sessionIds GROUP BY e.etudiantId")
  List<Object[]> countExercicesParEtudiant(@Param("sessionIds") List<Long> sessionIds);
}

