package com.concours.controller;

import com.concours.dto.SpecialiteDTO;
import com.concours.service.SpecialiteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specialites")
@CrossOrigin(origins = "*")
public class SpecialiteController {

    private final SpecialiteService specialiteService;

    public SpecialiteController(SpecialiteService specialiteService) {
        this.specialiteService = specialiteService;
    }

    @PostMapping
    public ResponseEntity<SpecialiteDTO> creerSpecialite(@RequestBody SpecialiteDTO specialiteDTO) {
        SpecialiteDTO created = specialiteService.creerSpecialite(specialiteDTO);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<Page<SpecialiteDTO>> getAllSpecialites(Pageable pageable) {
        Page<SpecialiteDTO> specialites = specialiteService.getAllSpecialites(pageable);
        return ResponseEntity.ok(specialites);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SpecialiteDTO>> getAllSpecialitesList() {
        List<SpecialiteDTO> specialites = specialiteService.getAllSpecialites();
        return ResponseEntity.ok(specialites);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecialiteDTO> getSpecialiteById(@PathVariable Long id) {
        SpecialiteDTO specialite = specialiteService.getSpecialiteById(id);
        return ResponseEntity.ok(specialite);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpecialiteDTO> modifierSpecialite(
            @PathVariable Long id, @RequestBody SpecialiteDTO specialiteDTO) {
        SpecialiteDTO updated = specialiteService.modifierSpecialite(id, specialiteDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerSpecialite(@PathVariable Long id) {
        specialiteService.supprimerSpecialite(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<SpecialiteDTO>> rechercherSpecialites(@RequestParam String terme) {
        List<SpecialiteDTO> specialites = specialiteService.rechercherSpecialites(terme);
        return ResponseEntity.ok(specialites);
    }
}