package com.concours.controller;

import com.concours.dto.CentreExamenDTO;
import com.concours.service.CentreExamenService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/centres-examen")
@CrossOrigin(origins = "*")
public class CentreExamenController {

    private final CentreExamenService centreExamenService;

    public CentreExamenController(CentreExamenService centreExamenService) {
        this.centreExamenService = centreExamenService;
    }

    @PostMapping
    public ResponseEntity<CentreExamenDTO> creerCentreExamen(@RequestBody CentreExamenDTO centreExamenDTO) {
        CentreExamenDTO created = centreExamenService.creerCentreExamen(centreExamenDTO);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<Page<CentreExamenDTO>> getAllCentresExamen(Pageable pageable) {
        Page<CentreExamenDTO> centres = centreExamenService.getAllCentresExamen(pageable);
        return ResponseEntity.ok(centres);
    }

    @GetMapping("/actifs")
    public ResponseEntity<List<CentreExamenDTO>> getCentresActifs() {
        List<CentreExamenDTO> centres = centreExamenService.getCentresActifs();
        return ResponseEntity.ok(centres);
    }

    @GetMapping("/ville/{villeId}")
    public ResponseEntity<List<CentreExamenDTO>> getCentresParVille(@PathVariable Long villeId) {
        List<CentreExamenDTO> centres = centreExamenService.getCentresParVille(villeId);
        return ResponseEntity.ok(centres);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CentreExamenDTO> getCentreExamenById(@PathVariable Long id) {
        CentreExamenDTO centre = centreExamenService.getCentreExamenById(id);
        return ResponseEntity.ok(centre);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CentreExamenDTO> modifierCentreExamen(
            @PathVariable Long id, @RequestBody CentreExamenDTO centreExamenDTO) {
        CentreExamenDTO updated = centreExamenService.modifierCentreExamen(id, centreExamenDTO);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<Void> activerCentreExamen(@PathVariable Long id) {
        centreExamenService.activerCentreExamen(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiverCentreExamen(@PathVariable Long id) {
        centreExamenService.desactiverCentreExamen(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerCentreExamen(@PathVariable Long id) {
        centreExamenService.supprimerCentreExamen(id);
        return ResponseEntity.noContent().build();
    }
}