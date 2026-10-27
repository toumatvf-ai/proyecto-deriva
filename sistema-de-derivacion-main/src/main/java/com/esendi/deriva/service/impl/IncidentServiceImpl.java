package com.esendi.deriva.service.impl;

import com.esendi.deriva.exception.ResourceNotFoundException;
import com.esendi.deriva.model.*;
import com.esendi.deriva.repository.*;
import com.esendi.deriva.service.IncidentService;
import org.apache.commons.lang3.RandomStringUtils;
import com.google.common.base.Preconditions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Year;
import java.util.List;

@Service
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentHistoryRepository incidentHistoryRepository;
    private final UserAccountRepository userAccountRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final IncidentStatusRepository incidentStatusRepository;

    public IncidentServiceImpl(
        IncidentRepository incidentRepository,
        IncidentHistoryRepository incidentHistoryRepository,
        UserAccountRepository userAccountRepository,
        CompanyRepository companyRepository,
        DepartmentRepository departmentRepository,
        IncidentStatusRepository incidentStatusRepository
    ) {
        this.incidentRepository = incidentRepository;
        this.incidentHistoryRepository = incidentHistoryRepository;
        this.userAccountRepository = userAccountRepository;
        this.companyRepository = companyRepository;
        this.departmentRepository = departmentRepository;
        this.incidentStatusRepository = incidentStatusRepository;
    }

    @Override
    @Transactional
    public Incident createIncident(Long clientId, Long companyId, String companyRuc, String companyName, String title, String description, String photoUrl) {
        
        Preconditions.checkArgument(title != null && !title.trim().isEmpty(), "El título no puede estar vacío");
        Preconditions.checkArgument(description != null && !description.trim().isEmpty(), "La descripción no puede estar vacía");

        UserAccount client = userAccountRepository.findWithCompaniesByUserAccountId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        
        Company companyToUse = null;

        if (companyId != null) {
            companyToUse = companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
            
            if (!client.getCompanies().contains(companyToUse)) {
                throw new IllegalArgumentException("La empresa seleccionada no pertenece a su cuenta.");
            }
        } else if (companyRuc != null && !companyRuc.trim().isEmpty() && companyName != null && !companyName.trim().isEmpty()) {
            companyToUse = companyRepository.findByTaxId(companyRuc).orElseGet(() -> {
                Company newCompany = new Company();
                newCompany.setTaxId(companyRuc);
                newCompany.setLegalName(companyName);
                return companyRepository.save(newCompany);
            });
            
            if (!client.getCompanies().contains(companyToUse)) {
                client.getCompanies().add(companyToUse);
                userAccountRepository.save(client);
            }
        } else {
            throw new IllegalArgumentException("Debe seleccionar una empresa o registrar una nueva.");
        }

        IncidentStatus statusPending = incidentStatusRepository.findByName("Pendiente")
                .orElseThrow(() -> new ResourceNotFoundException("Estado 'Pendiente' no configurado en BD"));

        Incident incident = new Incident();
        incident.setClient(client);
        incident.setCompany(companyToUse);
        incident.setIncidentStatus(statusPending);
        incident.setTitle(title);
        incident.setDescription(description);
        incident.setPhotoUrl(photoUrl);
        
        @SuppressWarnings("deprecation")
        String generatedCode = "TKT-" + Year.now().getValue() + "-" + RandomStringUtils.randomAlphanumeric(5).toUpperCase();
        incident.setCode(generatedCode);
        
        Incident savedIncident = incidentRepository.save(incident);
        saveHistory(savedIncident, client, null, statusPending, "Incidencia creada por el cliente", photoUrl);

        return savedIncident;
    }

    @Override
    @Transactional
    public Incident routeIncident(Long incidentId, Long advisorId, Integer departmentId, Long technicianId, String priority, String comment) {
        Incident incident = getIncidentById(incidentId);
        UserAccount advisor = userAccountRepository.findById(advisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Asesor no encontrado"));
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Área no encontrada"));
        UserAccount technician = userAccountRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado"));
        IncidentStatus statusAssigned = incidentStatusRepository.findByName("Asignado")
                .orElseThrow(() -> new ResourceNotFoundException("Estado 'Asignado' no configurado en BD"));

        IncidentStatus previousStatus = incident.getIncidentStatus();

        incident.setAdvisor(advisor);
        incident.setDepartment(department);
        incident.setAssignedTo(technician);
        incident.setPriority(priority);
        incident.setIncidentStatus(statusAssigned);
        
        Incident updatedIncident = incidentRepository.save(incident);
        saveHistory(updatedIncident, advisor, previousStatus, statusAssigned, comment, null);

        return updatedIncident;
    }

    @Override
    @Transactional
    public Incident rejectIncident(Long incidentId, Long advisorId, String comment) {
        Preconditions.checkArgument(comment != null && !comment.trim().isEmpty(), "Debe ingresar un motivo para rechazar el reclamo.");

        Incident incident = getIncidentById(incidentId);
        UserAccount advisor = userAccountRepository.findById(advisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Asesor no encontrado"));
        
        IncidentStatus statusRejected = incidentStatusRepository.findByName("Rechazado")
                .orElseThrow(() -> new ResourceNotFoundException("Estado 'Rechazado' no configurado en BD"));

        IncidentStatus previousStatus = incident.getIncidentStatus();

        incident.setAdvisor(advisor);
        incident.setIncidentStatus(statusRejected);
        
        Incident updatedIncident = incidentRepository.save(incident);
        saveHistory(updatedIncident, advisor, previousStatus, statusRejected, comment, null);

        return updatedIncident;
    }

    @Override
    @Transactional
    public Incident startIncidentProgress(Long incidentId, Long technicianId) {
        Incident incident = getIncidentById(incidentId);
        UserAccount technician = userAccountRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado"));
        IncidentStatus statusInProgress = incidentStatusRepository.findByName("En Proceso")
                .orElseThrow(() -> new ResourceNotFoundException("Estado 'En Proceso' no configurado en BD"));

        IncidentStatus previousStatus = incident.getIncidentStatus();

        incident.setIncidentStatus(statusInProgress);
        Incident updatedIncident = incidentRepository.save(incident);
        saveHistory(updatedIncident, technician, previousStatus, statusInProgress, "El área ha comenzado a trabajar en la incidencia", null);

        return updatedIncident;
    }

    @Override
    @Transactional
    public Incident resolveIncident(Long incidentId, Long technicianId, String resolutionComment, String evidenceUrl) {
        Incident incident = getIncidentById(incidentId);
        UserAccount technician = userAccountRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado"));
        IncidentStatus statusResolved = incidentStatusRepository.findByName("Solucionado")
                .orElseThrow(() -> new ResourceNotFoundException("Estado 'Solucionado' no configurado en BD"));

        IncidentStatus previousStatus = incident.getIncidentStatus();

        incident.setIncidentStatus(statusResolved);
        Incident updatedIncident = incidentRepository.save(incident);
        saveHistory(updatedIncident, technician, previousStatus, statusResolved, resolutionComment, evidenceUrl);

        return updatedIncident;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Incident> getIncidentsByClient(Long clientId) {
        return incidentRepository.findByClient_UserAccountIdOrderByCreatedAtDesc(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Incident> getPendingIncidents() {
        return incidentRepository.findByIncidentStatus_NameOrderByCreatedAtDesc("Pendiente");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Incident> getIncidentsByTechnician(Long technicianId) {
        return incidentRepository.findByAssignedTo_UserAccountIdOrderByCreatedAtDesc(technicianId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentHistory> getIncidentHistory(Long incidentId) {
        return incidentHistoryRepository.findByIncident_IncidentIdOrderByCreatedAtDesc(incidentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Incident getIncidentById(Long incidentId) {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada con ID: " + incidentId));
    }

    private void saveHistory(
        Incident incident, UserAccount actionUser, IncidentStatus previousStatus, 
        IncidentStatus newStatus, String comment, String evidenceUrl
    ) {
        IncidentHistory history = new IncidentHistory();
        history.setIncident(incident);
        history.setActionUser(actionUser);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setCommentText(comment);
        history.setEvidenceUrl(evidenceUrl);
        incidentHistoryRepository.save(history);
    }
}