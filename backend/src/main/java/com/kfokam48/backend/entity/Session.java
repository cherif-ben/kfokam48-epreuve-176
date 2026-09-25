package com.kfokam48.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;

/** Session de cours ouverte par un formateur (D2). Table {@code session} (V1). */
@Entity
@Table(name = "session")
public class Session {

  /** RG1 (Q2) — le code de présence est valide 15 minutes après l'ouverture. */
  public static final Duration DUREE_VALIDITE_CODE = Duration.ofMinutes(15);

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "titre", nullable = false, length = 255)
  private String titre;

  @Column(name = "promotion_id", nullable = false)
  private Long promotionId;

  @Column(name = "formateur_id", nullable = false)
  private Long formateurId;

  @Column(name = "code", nullable = false, length = 10, unique = true)
  private String code;

  @Column(name = "ouverture_at", nullable = false)
  private LocalDateTime ouvertureAt;

  @Column(name = "expiration_at", nullable = false)
  private LocalDateTime expirationAt;

  @Column(name = "cloture_at")
  private LocalDateTime clotureAt;

  protected Session() {
    // requis par JPA
  }

  public Session(
      String titre,
      Long promotionId,
      Long formateurId,
      String code,
      LocalDateTime ouvertureAt,
      LocalDateTime expirationAt) {
    this.titre = titre;
    this.promotionId = promotionId;
    this.formateurId = formateurId;
    this.code = code;
    this.ouvertureAt = ouvertureAt;
    this.expirationAt = expirationAt;
  }

  /** RG14 (Q3) — une session clôturée verrouille présences, dépôts et relectures. */
  public boolean estCloturee() {
    return clotureAt != null;
  }

  public boolean estExpiree(LocalDateTime maintenant) {
    return expirationAt.isBefore(maintenant);
  }

  /** EF11 — la clôture est définitive : elle ne peut être posée qu'une fois. */
  public void cloturer(LocalDateTime dateCloture) {
    this.clotureAt = dateCloture;
  }

  public Long getId() {
    return id;
  }

  public String getTitre() {
    return titre;
  }

  public Long getPromotionId() {
    return promotionId;
  }

  public Long getFormateurId() {
    return formateurId;
  }

  public String getCode() {
    return code;
  }

  public LocalDateTime getOuvertureAt() {
    return ouvertureAt;
  }

  public LocalDateTime getExpirationAt() {
    return expirationAt;
  }

  public LocalDateTime getClotureAt() {
    return clotureAt;
  }
}
