package com.esendi.deriva.repository;

import com.esendi.deriva.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    // Buscar un rol por su nombre exacto (ej. "CLIENTE", "ASESOR")
    Optional<Role> findByName(String name);
}