package com.iaspring.backspring.controller;

import com.iaspring.backspring.dto.CrudDto.UtilisateurRequest;
import com.iaspring.backspring.entity.Utilisateur;
import com.iaspring.backspring.repository.UtilisateurRepository;
import com.iaspring.backspring.security.JwtService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthRestController {

    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;

    @Data
    public static class LoginRequest {
        private String email;
        private String motDePasse;
    }

    // --- CONNEXION CLASSIQUE ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Utilisateur> optUser = utilisateurRepository.findByEmail(request.getEmail());

        if (optUser.isPresent() && optUser.get().getMotDePasse().equals(request.getMotDePasse())) {
            Utilisateur u = optUser.get();
            String token = jwtService.genererToken(u);
            
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("role", u.getRole());
            response.put("pseudo", u.getPseudo());
            response.put("utilisateurId", String.valueOf(u.getId()));
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).body(Map.of("error", "Identifiants incorrects"));
    }

    // --- INSCRIPTION CLASSIQUE ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UtilisateurRequest request) {
        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cet email est déjà utilisé."));
        }

        Utilisateur newUser = new Utilisateur();
        newUser.setPseudo(request.getPseudo());
        newUser.setEmail(request.getEmail());
        newUser.setMotDePasse(request.getMotDePasse()); 
        newUser.setRole("ROLE_USER");

        utilisateurRepository.save(newUser);
        return ResponseEntity.ok(Map.of("message", "Inscription réussie !"));
    }
}