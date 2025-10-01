package com.concours.controller;

import com.concours.dto.UtilisateurDTO;
import com.concours.entity.RoleUtilisateur;
import com.concours.service.UtilisateurService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin(origins = "*")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @PostMapping
    public ResponseEntity<UtilisateurDTO> creerUtilisateur(@RequestBody UtilisateurDTO utilisateurDTO) {
        UtilisateurDTO created = utilisateurService.creerUtilisateur(utilisateurDTO);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<Page<UtilisateurDTO>> getAllUtilisateurs(Pageable pageable) {
        Page<UtilisateurDTO> utilisateurs = utilisateurService.getAllUtilisateurs(pageable);
        return ResponseEntity.ok(utilisateurs);
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UtilisateurDTO>> getUtilisateursByRole(@PathVariable String role) {
        List<UtilisateurDTO> utilisateurs = utilisateurService.getUtilisateursByRole(RoleUtilisateur.valueOf(role));
        return ResponseEntity.ok(utilisateurs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> getUtilisateurById(@PathVariable Long id) {
        UtilisateurDTO utilisateur = utilisateurService.getUtilisateurById(id);
        return ResponseEntity.ok(utilisateur);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> modifierUtilisateur(
            @PathVariable Long id, @RequestBody UtilisateurDTO utilisateurDTO) {
        UtilisateurDTO updated = utilisateurService.modifierUtilisateur(id, utilisateurDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerUtilisateur(@PathVariable Long id) {
        utilisateurService.supprimerUtilisateur(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<Void> activerUtilisateur(@PathVariable Long id) {
        utilisateurService.activerUtilisateur(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiverUtilisateur(@PathVariable Long id) {
        utilisateurService.desactiverUtilisateur(id);
        return ResponseEntity.ok().build();
    }
}