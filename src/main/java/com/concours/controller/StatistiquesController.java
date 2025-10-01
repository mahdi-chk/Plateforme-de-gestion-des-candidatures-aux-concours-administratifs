package com.concours.controller;

import com.concours.dto.StatistiquesDTO;
import com.concours.service.StatistiquesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistiques")
@CrossOrigin(origins = "*")
public class StatistiquesController {

    private final StatistiquesService statistiquesService;

    public StatistiquesController(StatistiquesService statistiquesService) {
        this.statistiquesService = statistiquesService;
    }

    @GetMapping
    public ResponseEntity<StatistiquesDTO> getStatistiquesGlobales() {
        StatistiquesDTO stats = statistiquesService.getStatistiquesGlobales();
        return ResponseEntity.ok(stats);
    }
}