package com.esendi.deriva.service;

import com.esendi.deriva.model.Company;
import com.esendi.deriva.model.Department;
import com.esendi.deriva.model.IncidentStatus;
import java.util.List;

public interface CatalogService {
    List<Department> getAllDepartments();
    List<Company> getAllCompanies();
    IncidentStatus getStatusByName(String name);
    Department getDepartmentById(Integer departmentId);
}