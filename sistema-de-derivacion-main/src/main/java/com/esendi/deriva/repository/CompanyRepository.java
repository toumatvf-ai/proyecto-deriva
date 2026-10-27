package com.esendi.deriva.repository;

import com.esendi.deriva.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    // Buscar una empresa por su RUC (taxId)
    @Query("SELECT c FROM Company c WHERE c.taxId = :taxId")
    Optional<Company> findByTaxId(String taxId);
}