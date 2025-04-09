package com.bailoteca.repository.clase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bailoteca.models.clase.HorarioClase;

import java.util.List;

@Repository
public interface HorarioClaseRepo extends JpaRepository<HorarioClase, Long> {
    List<HorarioClase> findByClaseId(Long claseId);
}