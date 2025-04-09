package com.bailoteca.repository;

import com.bailoteca.models.HorarioClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioClaseRepo extends JpaRepository<HorarioClase, Long> {
    List<HorarioClase> findByClaseId(Long claseId);
}