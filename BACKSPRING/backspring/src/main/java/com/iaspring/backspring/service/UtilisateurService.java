package com.iaspring.backspring.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.iaspring.backspring.dto.CrudDto;
import com.iaspring.backspring.entity.Utilisateur;
import com.iaspring.backspring.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public CrudDto.UtilisateurResponse creer(CrudDto.UtilisateurRequest donnees) {
        if (utilisateurRepository.existsByEmail(donnees.getEmail()) || utilisateurRepository.existsByPseudo(donnees.getPseudo())) {
            throw new RuntimeException("Email ou pseudo déjà utilisé");
        }
        Utilisateur u = new Utilisateur();
        u.setPseudo(donnees.getPseudo());
        u.setEmail(donnees.getEmail());
        u.setMotDePasse(donnees.getMotDePasse()); 
        
        // 👉 NOUVEAU
        if (donnees.getRole() != null && !donnees.getRole().isEmpty()) {
            u.setRole(donnees.getRole());
        } else {
            u.setRole("ROLE_USER");
        }
        
        return mapperVersDto(utilisateurRepository.save(u));
    }

    // 👉 NOUVEAU MAPPER
    private CrudDto.UtilisateurResponse mapperVersDto(Utilisateur u) {
        return CrudDto.UtilisateurResponse.builder()
                .id(u.getId())
                .pseudo(u.getPseudo())
                .email(u.getEmail())
                .role(u.getRole()) // <-- ICI
                .dateInscription(u.getDateInscription())
                .build();
    }

    public List<CrudDto.UtilisateurResponse> recupererTous() {
        return utilisateurRepository.findAll().stream().map(this::mapperVersDto).collect(Collectors.toList());
    }

    public CrudDto.UtilisateurResponse recupererParId(Long id) {
        Utilisateur u = utilisateurRepository.findById(id).orElseThrow();
        return mapperVersDto(u);
    }

    public CrudDto.UtilisateurResponse modifier(Long id, CrudDto.UtilisateurRequest donnees) {
        Utilisateur existant = utilisateurRepository.findById(id).orElseThrow();
        existant.setPseudo(donnees.getPseudo());
        existant.setEmail(donnees.getEmail());
        if (donnees.getMotDePasse() != null && !donnees.getMotDePasse().isEmpty()) {
            existant.setMotDePasse(donnees.getMotDePasse());
        }
        return mapperVersDto(utilisateurRepository.save(existant));
    }

    public void supprimer(Long id) {
        utilisateurRepository.deleteById(id);
    }

 
}