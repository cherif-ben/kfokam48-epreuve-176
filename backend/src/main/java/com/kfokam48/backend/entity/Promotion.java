package com.kfokam48.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Promotion d'étudiants (D2). Table {@code promotion} (V1). */
@Entity
@Table(name = "promotion")
public class Promotion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "nom", nullable = false, length = 255)
  private String nom;

  protected Promotion() {
    // requis par JPA
  }

  public Promotion(String nom) {
    this.nom = nom;
  }

  public Long getId() {
    return id;
  }

  public String getNom() {
    return nom;
  }
}
