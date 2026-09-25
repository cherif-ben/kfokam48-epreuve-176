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
 * Relecture d'un exercice par un pair (D2). La contrainte {@code UNIQUE(exercice_id)} de `V1`
 * garantit RG4 (un seul relecteur par exercice) au niveau base de données.
 *
 * <p>Le relecteur ({@code relecteurId}) référence un {@code Etudiant} existant : conformément à la
 * décision de la section 2 du cahier des charges, le relecteur n'est pas un acteur distinct.
 */
@Entity
public class Relecture {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "exercice_id", nullable = false)
  private Long exerciceId;

  @Column(name = "relecteur_id", nullable = false)
  private Long relecteurId;

  @Column(name = "note")
  private Integer note;

  @Column(name = "commentaire")
  private String commentaire;

  @Enumerated(EnumType.STRING)
  @Column(name = "statut", nullable = false, length = 20)
  private StatutRelecture statut;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "rendue_at")
  private LocalDateTime rendueAt;

  @Column(name = "modifiee_at")
  private LocalDateTime modifieeAt;

  protected Relecture() {
    // requis par JPA
  }

  /**
   * EF8 / RG4 / RG5 — crée une relecture assignée à un relecteur, encore non rendue (RG8).
   *
   * @param exerciceId l'exercice à relire (unique, cf. contrainte de V1).
   * @param relecteurId le pair choisi, différent de l'auteur (RG2).
   * @param horodatage horodatage commun à created_at / updated_at.
   */
  public Relecture(Long exerciceId, Long relecteurId, LocalDateTime horodatage) {
    this.exerciceId = exerciceId;
    this.relecteurId = relecteurId;
    this.statut = StatutRelecture.EN_ATTENTE;
    this.createdAt = horodatage;
    this.updatedAt = horodatage;
  }

  /** EF10 (Q10) — le relecteur rend sa relecture avec une note (0-20) et un commentaire. */
  public void rendre(Integer note, String commentaire, LocalDateTime horodatage) {
    this.note = note;
    this.commentaire = commentaire;
    this.statut = StatutRelecture.RENDUE;
    this.rendueAt = horodatage;
    this.updatedAt = horodatage;
  }

  /**
   * EF10 / RG11 (Q10) — correction d'une note déjà rendue, avant la clôture de la session.
   *
   * <p>La première reddition ({@code rendueAt}) est préservée : elle atteste du moment où la note a
   * été envoyée pour la première fois. Seul {@code modifieeAt} est mis à jour, pour tracer la
   * dernière correction. Le statut reste {@code RENDUE} (RG12 : la note est définitive à la
   * clôture, Q10 > Q15).
   *
   * @param horodatage horodatage de la correction.
   */
  public void corriger(Integer note, String commentaire, LocalDateTime horodatage) {
    this.note = note;
    this.commentaire = commentaire;
    this.modifieeAt = horodatage;
    this.updatedAt = horodatage;
  }

  public Long getId() {
    return id;
  }

  public Long getExerciceId() {
    return exerciceId;
  }

  public Long getRelecteurId() {
    return relecteurId;
  }

  public Integer getNote() {
    return note;
  }

  public String getCommentaire() {
    return commentaire;
  }

  public StatutRelecture getStatut() {
    return statut;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public LocalDateTime getRendueAt() {
    return rendueAt;
  }

  public LocalDateTime getModifieeAt() {
    return modifieeAt;
  }
}
