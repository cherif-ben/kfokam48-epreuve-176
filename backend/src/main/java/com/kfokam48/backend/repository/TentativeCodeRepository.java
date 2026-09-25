package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.TentativeCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Accès au compteur d'échecs RG13 (Q4). */
public interface TentativeCodeRepository extends JpaRepository<TentativeCode, Long> {

  Optional<TentativeCode> findBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

  /** Supprime une entrée après une saisie réussie (réinitialisation par récréation). */
  void deleteBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

  /** Réinitialise le compteur après une saisie réussie (RG13). */
  @Modifying
  @Query(
      "UPDATE TentativeCode t SET t.echecs = 0, t.blocageJusqua = null, t.updatedAt = :horodatage "
          + "WHERE t.sessionId = :sessionId AND t.etudiantId = :etudiantId")
  int reinitialiser(
      @Param("sessionId") Long sessionId,
      @Param("etudiantId") Long etudiantId,
      @Param("horodatage") java.time.LocalDateTime horodatage);
}
