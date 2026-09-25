package com.kfokam48.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Exercice déposé par un étudiant pour une session (D2). La contrainte
 * {@code UNIQUE(session_id, etudiant_id)} de `V1` garantit RG9.
 */
@Entity
@Table(name = "exercice")
public class Exercice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false)
  private Long sessionId;

  @Column(name = "etudiant_id", nullable = false)
  private Long etudiantId;

  @Column(name = "lien", nullable = false, length = 2048)
  private String lien;

  @Enumerated(EnumType.STRING)
  @Column(name = "statut", nullable = false, length = 30)
  private StatutExercice statut;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  protected Exercice() {
    // requis par JPA
  }

  public Exercice(
      Long sessionId, Long etudiantId, String lien, StatutExercice statut, LocalDateTime horodatage) {
    this.sessionId = sessionId;
    this.etudiantId = etudiantId;
    this.lien = lien;
    this.statut = statut;
    this.createdAt = horodatage;
    this.updatedAt = horodatage;
  }

  /** EF7 / RG10 (Q13) — le lien est remplaçable tant que la relecture n'a pas commencé. */
  public void remplacerLien(String nouveauLien, LocalDateTime horodatage) {
    this.lien = nouveauLien;
    this.updatedAt = horodatage;
  }

  /** EF9 — la relecture est rendue : l'exercice passe à {@code RELU}. */
  public void marquerRelu(LocalDateTime horodatage) {
    this.statut = StatutExercice.RELU;
    this.updatedAt = horodatage;
  }

  /** Q11 — aucun relecteur éligible : l'exercice reste en attente de relecture. */
  public void marquerEnAttenteDeRelecture(LocalDateTime horodatage) {
    this.statut = StatutExercice.EN_ATTENTE_RELECTURE;
    this.updatedAt = horodatage;
  }

  public Long getId() {
    return id;
  }

  public Long getSessionId() {
    return sessionId;
  }

  public Long getEtudiantId() {
    return etudiantId;
  }

  public String getLien() {
    return lien;
  }

  public StatutExercice getStatut() {
    return statut;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
