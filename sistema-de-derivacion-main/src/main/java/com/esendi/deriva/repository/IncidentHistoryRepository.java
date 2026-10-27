package com.esendi.deriva.repository;

import com.esendi.deriva.model.IncidentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncidentHistoryRepository extends JpaRepository<IncidentHistory, Long> {
    
    // Para la Trazabilidad: Obtener toda la línea de tiempo de un reclamo específico, 
    // ordenado desde el más reciente al más antiguo.
    List<IncidentHistory> findByIncident_IncidentIdOrderByCreatedAtDesc(Long incidentId);
}