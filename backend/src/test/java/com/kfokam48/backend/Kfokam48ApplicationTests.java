package com.kfokam48.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Kfokam48ApplicationTests {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void contextLoads() {
    // L'application démarre : Flyway a exécuté les migrations sur H2 (B5, B6)
  }

  @Test
  void routeInconnue_repond404() throws Exception {
    mockMvc.perform(get("/api/inexistant")).andExpect(status().isNotFound());
  }
}
