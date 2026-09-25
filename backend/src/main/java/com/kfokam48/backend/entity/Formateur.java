package com.kfokam48.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Formateur qui ouvre une session (D2). Table {@code formateur} (V1). */
@Entity
@Table(name = "formateur")
public class Formateur {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "nom", nullable = false, length = 255)
  private String nom;

  protected Formateur() {
    // requis par JPA
  }

  public Formateur(String nom) {
    this.nom = nom;
  }

  public Long getId() {
    return id;
  }

  public String getNom() {
    return nom;
  }
}
