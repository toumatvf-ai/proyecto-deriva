package com.esendi.deriva.repository;

import com.esendi.deriva.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    
    // Buscar un área por su nombre (ej. "Calidad", "Software")
    Optional<Department> findByName(String name);
}