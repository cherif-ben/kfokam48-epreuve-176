package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.Formateur;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux formateurs (B3). */
public interface FormateurRepository extends JpaRepository<Formateur, Long> {

  Optional<Formateur> findFirstByOrderByIdAsc();
}
