package com.esendi.deriva.repository;

import com.esendi.deriva.model.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    
    @EntityGraph(attributePaths = {"roles", "department"})
    Optional<UserAccount> findByEmail(String email);
    
    @EntityGraph(attributePaths = {"companies"})
    Optional<UserAccount> findWithCompaniesByUserAccountId(Long userAccountId);
    
    List<UserAccount> findByDepartment_DepartmentId(Integer departmentId);
    
    List<UserAccount> findByRoles_Name(String roleName);
    
    boolean existsByEmail(String email);

    // Traer usuario con sus roles y departamento para la edición del Admin
    @EntityGraph(attributePaths = {"roles", "department"})
    Optional<UserAccount> findWithDetailsByUserAccountId(Long userAccountId);
}