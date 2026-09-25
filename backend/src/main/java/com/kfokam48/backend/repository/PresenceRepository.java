package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.Presence;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Accès aux présences (B3). */
public interface PresenceRepository extends JpaRepository<Presence, Long> {

  boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

  List<Presence> findBySessionId(Long sessionId);

  long countByEtudiantId(Long etudiantId);

  /** EF12 (Q16) — comptage de présences par étudiant, pour les sessions d'une promotion. */
  @Query(
      "SELECT p.etudiantId, COUNT(p) FROM Presence p WHERE p.sessionId IN :sessionIds GROUP BY p.etudiantId")
  List<Object[]> countPresencesParEtudiant(@Param("sessionIds") List<Long> sessionIds);
}

