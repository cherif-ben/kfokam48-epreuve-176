package com.kfokam48.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * RG13 (Q4) — compteur d'échecs de saisie de code par couple (étudiant, session).
 *
 * <p>Après 5 échecs consécutifs, la saisie est bloquée pendant 2 minutes. Une saisie réussie
 * réinitialise le compteur. Le blocage est propre à chaque couple, pas global.
 */
@Entity
@Table(name = "tentative_code")
public class TentativeCode {

  /** RG13 (Q4) — délai de blocage après le 5e échec consécutif. */
  public static final int SEUIL_ECHECS = 5;

  public static final Duration DELAI_BLOCAGE = Duration.ofMinutes(2);

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false)
  private Long sessionId;

  @Column(name = "etudiant_id", nullable = false)
  private Long etudiantId;

  @Column(name = "echecs", nullable = false)
  private int echecs;

  @Column(name = "blocage_jusqua")
  private LocalDateTime blocageJusqua;

  @Column(name = "dernier_echec", nullable = false)
  private LocalDateTime dernierEchec;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  protected TentativeCode() {
    // requis par JPA
  }

  public TentativeCode(Long sessionId, Long etudiantId, LocalDateTime horodatage) {
    this.sessionId = sessionId;
    this.etudiantId = etudiantId;
    this.echecs = 0;
    this.blocageJusqua = null;
    this.dernierEchec = horodatage;
    this.createdAt = horodatage;
    this.updatedAt = horodatage;
  }

  /** Indique si la saisie est actuellement bloquée (RG13 : 2 min après le 5e échec). */
  public boolean estBloquee(LocalDateTime maintenant) {
    return blocageJusqua != null && blocageJusqua.isAfter(maintenant);
  }

  /** Nombre de secondes restantes avant le déblocage automatique (0 si non bloqué ou bloqué passé). */
  public long secondesRestantes(LocalDateTime maintenant) {
    if (blocageJusqua == null || blocageJusqua.isBefore(maintenant) || blocageJusqua.equals(maintenant)) {
      return 0;
    }
    return Duration.between(maintenant, blocageJusqua).toSeconds();
  }

  /** Enregistre un échec : incrémente le compteur et pose le blocage au 5e échec (RG13). */
  public void enregistrerEchec(LocalDateTime horodatage) {
    this.echecs += 1;
    this.dernierEchec = horodatage;
    this.updatedAt = horodatage;
    if (this.echecs >= SEUIL_ECHECS) {
      this.blocageJusqua = horodatage.plus(DELAI_BLOCAGE);
    }
  }

  /** Réinitialise le compteur après une saisie réussie (RG13). */
  public void reinitialiser(LocalDateTime horodatage) {
    this.echecs = 0;
    this.blocageJusqua = null;
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

  public int getEchecs() {
    return echecs;
  }

  public LocalDateTime getBlocageJusqua() {
    return blocageJusqua;
  }

  public LocalDateTime getDernierEchec() {
    return dernierEchec;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
