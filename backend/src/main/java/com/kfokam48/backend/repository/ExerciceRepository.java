package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.Exercice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux exercices (B3). */
public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

  boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

  Optional<Exercice> findBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

  List<Exercice> findByEtudiantIdOrderByCreatedAtDesc(Long etudiantId);

  List<Exercice> findByEtudiantIdAndSessionIdOrderByCreatedAtDesc(Long etudiantId, Long sessionId);

  List<Exercice> findBySessionId(Long sessionId);

  long countByEtudiantId(Long etudiantId);
}
