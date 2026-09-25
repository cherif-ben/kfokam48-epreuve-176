package com.kfokam48.backend.web;

import com.kfokam48.backend.erreur.ErreurMetierException;
import com.kfokam48.backend.erreur.ErreurResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * B4 — gestion centralisée des erreurs. Toutes les réponses d'erreur sortent au format
 * {@code { code, message }} du contrat, sans stack trace.
 *
 * <p>L'advice étend {@link ResponseEntityExceptionHandler} pour conserver les statuts HTTP
 * standards (404, 405, 415…) tout en normalisant leur corps.
 */
@RestControllerAdvice
public class GestionnaireErreurs extends ResponseEntityExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(GestionnaireErreurs.class);
  private static final String MESSAGE_ERREUR_INTERNE =
      "Une erreur interne est survenue. Réessayez dans un instant.";

  /** Toutes les exceptions métier portent leur code et leur statut (voir {@code erreur/}). */
  @ExceptionHandler(ErreurMetierException.class)
  public ResponseEntity<Object> gererErreurMetier(ErreurMetierException erreur) {
    return ResponseEntity.status(erreur.getStatut())
        .body(new ErreurResponse(erreur.getCode(), erreur.getMessage()));
  }

  /** Filet de sécurité des contraintes UNIQUE / CHECK (RG3, RG9, RG12) non interceptées par le service. */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Object> gererConflitDeDonnees(DataIntegrityViolationException erreur) {
    LOG.warn("Contrainte de base violée : {}", erreur.getMostSpecificCause().getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            new ErreurResponse(
                "CONFLIT_DONNEES", "La requête entre en conflit avec une contrainte d'intégrité."));
  }

  /** Dernier recours : le détail technique reste dans les logs du serveur, jamais dans la réponse. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> gererErreurInattendue(Exception erreur) {
    LOG.error("Erreur inattendue non gérée", erreur);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErreurResponse("ERREUR_INTERNE", MESSAGE_ERREUR_INTERNE));
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException erreur,
      HttpHeaders headers,
      HttpStatusCode statut,
      WebRequest request) {
    String message =
        erreur.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(
                champ ->
                    "Le champ « %s » est invalide : %s"
                        .formatted(champ.getField(), champ.getDefaultMessage()))
            .orElse("Un champ obligatoire est manquant.");
    return ResponseEntity.badRequest().body(new ErreurResponse("CHAMP_MANQUANT", message));
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException erreur,
      HttpHeaders headers,
      HttpStatusCode statut,
      WebRequest request) {
    return ResponseEntity.badRequest()
        .body(
            new ErreurResponse(
                "CORPS_INVALIDE", "Le corps de la requête est absent ou mal formé (JSON attendu)."));
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException erreur,
      HttpHeaders headers,
      HttpStatusCode statut,
      WebRequest request) {
    return ResponseEntity.badRequest()
        .body(
            new ErreurResponse(
                "CHAMP_MANQUANT",
                "Le paramètre « %s » est obligatoire.".formatted(erreur.getParameterName())));
  }

  @Override
  protected ResponseEntity<Object> handleTypeMismatch(
      TypeMismatchException erreur,
      HttpHeaders headers,
      HttpStatusCode statut,
      WebRequest request) {
    return ResponseEntity.badRequest()
        .body(
            new ErreurResponse(
                "PARAMETRE_INVALIDE",
                "Le paramètre « %s » est mal formé.".formatted(erreur.getPropertyName())));
  }

  /** Normalisation du corps de toutes les erreurs standard de Spring (404, 405, 415…). */
  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception erreur,
      Object body,
      HttpHeaders headers,
      HttpStatusCode statut,
      WebRequest request) {
    return new ResponseEntity<>(
        new ErreurResponse(codePour(statut), messagePour(statut)), headers, statut);
  }

  private static String codePour(HttpStatusCode statut) {
    return switch (statut.value()) {
      case 400 -> "REQUETE_INVALIDE";
      case 403 -> "ACCES_REFUSE";
      case 404 -> "RESSOURCE_INCONNUE";
      case 405 -> "METHODE_NON_AUTORISEE";
      case 406 -> "FORMAT_NON_ACCEPTABLE";
      case 415 -> "TYPE_MEDIA_NON_SUPPORTE";
      case 429 -> "TROP_DE_REQUETES";
      default -> statut.is5xxServerError() ? "ERREUR_INTERNE" : "ERREUR_REQUETE";
    };
  }

  private static String messagePour(HttpStatusCode statut) {
    return switch (statut.value()) {
      case 404 -> "La ressource demandée n'existe pas.";
      case 405 -> "Cette méthode HTTP n'est pas autorisée sur cette ressource.";
      case 406 -> "Le format demandé n'est pas acceptable.";
      case 415 -> "Le type de média envoyé n'est pas supporté.";
      default ->
          statut.is5xxServerError() ? MESSAGE_ERREUR_INTERNE : "La requête n'a pas pu être traitée.";
    };
  }
}
