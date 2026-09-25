package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.Presence;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux présences (B3). */
public interface PresenceRepository extends JpaRepository<Presence, Long> {

  boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

  List<Presence> findBySessionId(Long sessionId);

  long countByEtudiantId(Long etudiantId);
}
