package com.concours.controller;

import com.concours.entity.Ville;
import com.concours.service.VilleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/villes")
@CrossOrigin(origins = "*")
public class VilleController {

    private final VilleService villeService;

    public VilleController(VilleService villeService) {
        this.villeService = villeService;
    }

    @PostMapping
    public ResponseEntity<Ville> creerVille(@RequestParam String nom) {
        Ville ville = villeService.creerVille(nom);
        return ResponseEntity.ok(ville);
    }

    @GetMapping
    public ResponseEntity<List<Ville>> getAllVilles() {
        List<Ville> villes = villeService.getAllVilles();
        return ResponseEntity.ok(villes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ville> getVilleById(@PathVariable Long id) {
        Ville ville = villeService.getVilleById(id);
        return ResponseEntity.ok(ville);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Ville>> rechercherVilles(@RequestParam String terme) {
        List<Ville> villes = villeService.rechercherVilles(terme);
        return ResponseEntity.ok(villes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ville> modifierVille(
            @PathVariable Long id, @RequestParam String nouveauNom) {
        Ville ville = villeService.modifierVille(id, nouveauNom);
        return ResponseEntity.ok(ville);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerVille(@PathVariable Long id) {
        villeService.supprimerVille(id);
        return ResponseEntity.noContent().build();
    }
}