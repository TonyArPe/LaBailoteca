package com.bailoteca.repository.clase;

import com.bailoteca.models.clase.HorarioClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioClaseRepo extends JpaRepository<HorarioClase, Long> {

    List<HorarioClase> findByClaseId(Long claseId);

    @Modifying
    @Transactional
    void deleteByClaseId(Long claseId);
}
