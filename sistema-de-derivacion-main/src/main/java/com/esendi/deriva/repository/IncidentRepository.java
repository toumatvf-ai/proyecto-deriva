package com.esendi.deriva.repository;

import com.esendi.deriva.model.Incident;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    
    @EntityGraph(attributePaths = {"client", "company", "advisor", "department", "incidentStatus", "assignedTo"})
    Optional<Incident> findById(Long id);

    // Ordenados del más reciente al más antiguo
    List<Incident> findByClient_UserAccountIdOrderByCreatedAtDesc(Long clientId);
    
    List<Incident> findByIncidentStatus_NameOrderByCreatedAtDesc(String statusName);
    
    // NUEVO: Filtro estricto para que el técnico solo vea lo que se le asignó a él
    List<Incident> findByAssignedTo_UserAccountIdOrderByCreatedAtDesc(Long technicianId);
}