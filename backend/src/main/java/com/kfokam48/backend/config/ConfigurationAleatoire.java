package com.kfokam48.backend.config;

import java.util.Random;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Aléa injectable : RG5 (Q7) tire le relecteur au hasard parmi les étudiants présents. Un
 * {@code Random} injecté (et non {@code ThreadLocalRandom.current()}) permet aux tests unitaires
 * de figer le tir par un seed, sans attendre de hasard réel.
 */
@Configuration
public class ConfigurationAleatoire {

  @Bean
  public Random aleatoire() {
    return new Random();
  }
}
