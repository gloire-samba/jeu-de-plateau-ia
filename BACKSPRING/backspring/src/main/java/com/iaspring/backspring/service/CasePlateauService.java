package com.iaspring.backspring.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.iaspring.backspring.dto.CasePlateauDto;
import com.iaspring.backspring.entity.CasePlateauEntity;
import com.iaspring.backspring.entity.Partie;
import com.iaspring.backspring.repository.CasePlateauRepository;
import com.iaspring.backspring.repository.PartieRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CasePlateauService {

    private final CasePlateauRepository casePlateauRepository;
    private final PartieRepository partieRepository;

    // 1. Sauvegarder d'un coup tout le plateau généré
    @Transactional
    public void sauvegarderPlateauComplet(Long partieId, Map<Integer, MoteurJeuService.CasePlateau> plateau) {
        Partie partie = partieRepository.findById(partieId).orElseThrow();
        casePlateauRepository.deleteByPartieId(partieId); // Nettoie l'ancien plateau si on recommence

        for (Map.Entry<Integer, MoteurJeuService.CasePlateau> entry : plateau.entrySet()) {
            CasePlateauEntity entity = new CasePlateauEntity();
            entity.setPartie(partie);
            entity.setPositionPlateau(entry.getKey());
            entity.setEffet(entry.getValue().getEffet());
            entity.setCategorie(entry.getValue().getCategorie());
            entity.setPoints(entry.getValue().getPoints());
            casePlateauRepository.save(entity);
        }
    }

    // 2. Recharger le plateau depuis la base pour le donner au MoteurJeuService
    public Map<Integer, MoteurJeuService.CasePlateau> chargerPlateauPourMoteur(Long partieId) {
        List<CasePlateauEntity> cases = casePlateauRepository.findByPartieId(partieId);
        return cases.stream().collect(Collectors.toMap(
            CasePlateauEntity::getPositionPlateau,
            c -> new MoteurJeuService.CasePlateau(c.getEffet(), c.getCategorie(), c.getPoints())
        ));
    }

    // 3. CRUD Basique (Créer une case unique)
    @Transactional
    public CasePlateauDto creerCase(CasePlateauDto dto) {
        Partie partie = partieRepository.findById(dto.getPartieId()).orElseThrow();
        CasePlateauEntity entity = new CasePlateauEntity();
        entity.setPartie(partie);
        entity.setPositionPlateau(dto.getPositionPlateau());
        entity.setEffet(dto.getEffet());
        entity.setCategorie(dto.getCategorie());
        entity.setPoints(dto.getPoints());
        entity = casePlateauRepository.save(entity);
        dto.setId(entity.getId());
        return dto;
    }

    // 4. CRUD Basique (Lire les cases d'une partie pour Angular)
    public List<CasePlateauDto> getPlateauByPartie(Long partieId) {
        return casePlateauRepository.findByPartieId(partieId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // 👉 NOUVEAU : Lire absolument TOUTES les cases (pour le Dashboard Admin)
    public List<CasePlateauDto> getToutesLesCases() {
        return casePlateauRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // 👉 NOUVEAU : Supprimer une case (pour l'Admin)
    @Transactional
    public void supprimerCase(Long id) {
        casePlateauRepository.deleteById(id);
    }

    private CasePlateauDto mapToDto(CasePlateauEntity entity) {
        CasePlateauDto dto = new CasePlateauDto();
        dto.setId(entity.getId());
        dto.setPartieId(entity.getPartie().getId());
        dto.setPositionPlateau(entity.getPositionPlateau());
        dto.setEffet(entity.getEffet());
        dto.setCategorie(entity.getCategorie());
        dto.setPoints(entity.getPoints());
        return dto;
    }
}