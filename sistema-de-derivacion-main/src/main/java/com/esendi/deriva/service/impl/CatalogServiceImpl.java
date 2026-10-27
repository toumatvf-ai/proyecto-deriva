package com.esendi.deriva.service.impl;

import com.esendi.deriva.exception.ResourceNotFoundException;
import com.esendi.deriva.model.Company;
import com.esendi.deriva.model.Department;
import com.esendi.deriva.model.IncidentStatus;
import com.esendi.deriva.repository.CompanyRepository;
import com.esendi.deriva.repository.DepartmentRepository;
import com.esendi.deriva.repository.IncidentStatusRepository;
import com.esendi.deriva.service.CatalogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogService {

    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final IncidentStatusRepository incidentStatusRepository;

    public CatalogServiceImpl(
        DepartmentRepository departmentRepository,
        CompanyRepository companyRepository, 
        IncidentStatusRepository incidentStatusRepository
    ) {
        this.departmentRepository = departmentRepository;
        this.companyRepository = companyRepository;
        this.incidentStatusRepository = incidentStatusRepository;
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Override
    public IncidentStatus getStatusByName(String name) {
        return incidentStatusRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Estado de incidencia no encontrado: " + name));
    }

    @Override
    public Department getDepartmentById(Integer departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Área no encontrada con ID: " + departmentId));
    }
}