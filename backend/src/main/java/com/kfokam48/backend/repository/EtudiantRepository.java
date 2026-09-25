package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.Etudiant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux étudiants (B3). */
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

  List<Etudiant> findByPromotionIdOrderByNomAsc(Long promotionId);

  long countByPromotionId(Long promotionId);
}
