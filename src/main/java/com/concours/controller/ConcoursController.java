package com.concours.controller;

import com.concours.dto.ConcoursDTO;
import com.concours.service.ConcoursService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/concours")
@CrossOrigin(origins = "*")
public class ConcoursController {

    private final ConcoursService concoursService;

    public ConcoursController(ConcoursService concoursService) {
        this.concoursService = concoursService;
    }

    @PostMapping
    public ResponseEntity<ConcoursDTO> creerConcours(@RequestBody ConcoursDTO concoursDTO) {
        ConcoursDTO created = concoursService.creerConcours(concoursDTO);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/ouverts")
    public ResponseEntity<List<ConcoursDTO>> getConcoursOuverts() {
        List<ConcoursDTO> concours = concoursService.getConcoursOuverts();
        return ResponseEntity.ok(concours);
    }

    @GetMapping
    public ResponseEntity<Page<ConcoursDTO>> getAllConcours(Pageable pageable) {
        Page<ConcoursDTO> concours = concoursService.getAllConcours(pageable);
        return ResponseEntity.ok(concours);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConcoursDTO> getConcoursById(@PathVariable Long id) {
        ConcoursDTO concours = concoursService.getConcoursById(id);
        return ResponseEntity.ok(concours);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConcoursDTO> modifierConcours(
            @PathVariable Long id, @RequestBody ConcoursDTO concoursDTO) {
        ConcoursDTO updated = concoursService.modifierConcours(id, concoursDTO);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/publier")
    public ResponseEntity<Void> publierConcours(@PathVariable Long id) {
        concoursService.publierConcours(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerConcours(@PathVariable Long id) {
        concoursService.supprimerConcours(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ConcoursDTO>> searchConcoursOuverts(@RequestParam String searchTerm) {
        List<ConcoursDTO> concours = concoursService.searchConcoursOuverts(searchTerm);
        return ResponseEntity.ok(concours);
    }

    @GetMapping("/specialite/{specialiteId}")
    public ResponseEntity<List<ConcoursDTO>> getConcoursParSpecialite(@PathVariable Long specialiteId) {
        List<ConcoursDTO> concours = concoursService.getConcoursParSpecialite(specialiteId);
        return ResponseEntity.ok(concours);
    }
}