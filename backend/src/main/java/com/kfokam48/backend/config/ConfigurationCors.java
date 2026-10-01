package com.kfokam48.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration CORS — le frontend (http://localhost:3000) appelle l'API (http://localhost:8080)
 * directement depuis le navigateur : requêtes cross-origin, bloquées par défaut sans en-têtes
 * CORS (curl ne les applique pas, ce qui masque le problème côté tests d'API).
 *
 * <p>Les origines autorisées sont paramétrables via {@code app.cors.origines-autorisees}
 * (par défaut : http://localhost:3000, suffisant en local comme en Docker Compose où le
 * navigateur accède toujours au frontend par localhost:3000).
 */
@Configuration
public class ConfigurationCors implements WebMvcConfigurer {

  private final String[] originesAutorisees;

  public ConfigurationCors(
      @Value("${app.cors.origines-autorisees:http://localhost:3000}")
      String[] originesAutorisees) {
    this.originesAutorisees = originesAutorisees;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins(originesAutorisees)
        // PATCH : EF11 clôture de session ; OPTIONS : requête préliminaire (preflight) du navigateur.
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*");
  }
}
