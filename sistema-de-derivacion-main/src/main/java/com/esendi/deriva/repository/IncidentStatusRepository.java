package com.esendi.deriva.repository;

import com.esendi.deriva.model.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IncidentStatusRepository extends JpaRepository<IncidentStatus, Integer> {
    
    // Buscar un estado por su nombre (ej. "Pendiente", "En Proceso", "Solucionado")
    Optional<IncidentStatus> findByName(String name);
}