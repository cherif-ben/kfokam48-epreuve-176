package com.kfokam48.backend.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Horloge injectable : les règles dépendant du temps (RG1 expiration du code à 15 min, RG13
 * blocage de 2 min, RG14 clôture) restent testables sans attendre réellement.
 */
@Configuration
public class ConfigurationHorloge {

  @Bean
  public Clock horloge() {
    return Clock.systemDefaultZone();
  }
}
