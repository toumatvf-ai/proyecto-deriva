package com.esendi.deriva.service;

import com.esendi.deriva.model.Incident;
import com.esendi.deriva.model.IncidentHistory;
import java.util.List;

public interface IncidentService {
    Incident createIncident(Long clientId, Long companyId, String companyRuc, String companyName, String title, String description, String photoUrl);
    Incident routeIncident(Long incidentId, Long advisorId, Integer departmentId, Long technicianId, String priority, String comment);
    Incident rejectIncident(Long incidentId, Long advisorId, String comment);
    Incident startIncidentProgress(Long incidentId, Long technicianId);
    Incident resolveIncident(Long incidentId, Long technicianId, String resolutionComment, String evidenceUrl);
    
    List<Incident> getIncidentsByClient(Long clientId);
    List<Incident> getPendingIncidents();
    
    // NUEVO: Obtener incidencias por técnico
    List<Incident> getIncidentsByTechnician(Long technicianId);
    
    List<IncidentHistory> getIncidentHistory(Long incidentId);
    Incident getIncidentById(Long incidentId);
}