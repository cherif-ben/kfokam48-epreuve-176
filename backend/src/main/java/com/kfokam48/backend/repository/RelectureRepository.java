package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.Relecture;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux relectures (B3). */
public interface RelectureRepository extends JpaRepository<Relecture, Long> {

  /** RG4 (Q6) : un seul relecteur par exercice — recherche par clé naturelle. */
  Optional<Relecture> findByExerciceId(Long exerciceId);

  boolean existsByExerciceId(Long exerciceId);
}
