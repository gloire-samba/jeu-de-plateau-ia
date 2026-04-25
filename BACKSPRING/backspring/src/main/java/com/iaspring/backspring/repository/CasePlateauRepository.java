package com.iaspring.backspring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iaspring.backspring.entity.CasePlateauEntity;

public interface CasePlateauRepository extends JpaRepository<CasePlateauEntity, Long> {
    List<CasePlateauEntity> findByPartieId(Long partieId);
    void deleteByPartieId(Long partieId);
}