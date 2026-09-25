package com.kfokam48.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Étudiant d'une promotion (D2). Table {@code etudiant} (V1). */
@Entity
@Table(name = "etudiant")
public class Etudiant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "nom", nullable = false, length = 255)
  private String nom;

  @Column(name = "promotion_id", nullable = false)
  private Long promotionId;

  protected Etudiant() {
    // requis par JPA
  }

  public Etudiant(String nom, Long promotionId) {
    this.nom = nom;
    this.promotionId = promotionId;
  }

  public Long getId() {
    return id;
  }

  public String getNom() {
    return nom;
  }

  public Long getPromotionId() {
    return promotionId;
  }
}
