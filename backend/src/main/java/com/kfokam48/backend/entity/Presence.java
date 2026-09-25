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
 * Présence d'un étudiant à une session (D2). La contrainte {@code UNIQUE(session_id, etudiant_id)}
 * de `V1` garantit RG12 au niveau base de données.
 */
@Entity
@Table(name = "presence")
public class Presence {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false)
  private Long sessionId;

  @Column(name = "etudiant_id", nullable = false)
  private Long etudiantId;

  @Enumerated(EnumType.STRING)
  @Column(name = "source", nullable = false, length = 20)
  private SourcePresence source;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  protected Presence() {
    // requis par JPA
  }

  public Presence(Long sessionId, Long etudiantId, SourcePresence source, LocalDateTime createdAt) {
    this.sessionId = sessionId;
    this.etudiantId = etudiantId;
    this.source = source;
    this.createdAt = createdAt;
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

  public SourcePresence getSource() {
    return source;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
