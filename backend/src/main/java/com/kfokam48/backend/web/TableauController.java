package com.kfokam48.backend.web;

import com.kfokam48.backend.dto.TableauLigneResponse;
import com.kfokam48.backend.service.TableauService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** EF12 — contrôleur du tableau récapitulatif du formateur (B3). */
@RestController
@RequestMapping("/api/tableau")
public class TableauController {

  private final TableauService tableauService;

  public TableauController(TableauService tableauService) {
    this.tableauService = tableauService;
  }

  /** EF12 / RG8 (Q8, Q11, Q16) — le formateur consulte le récapitulatif de sa promotion. */
  @GetMapping
  public List<TableauLigneResponse> tableau(@RequestParam Long promotionId) {
    return tableauService.tableau(promotionId);
  }
}
