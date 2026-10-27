package com.esendi.deriva;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.esendi.deriva.model.Role;
import com.esendi.deriva.model.UserAccount;

@SpringBootTest
class DerivaApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring Boot cargue correctamente sin errores fatales
        assertTrue(true);
    }

    @Test
    void testUserAccountEntityCreation() {
        // Prueba Unitaria (TDD) básica para verificar la lógica de la entidad
        UserAccount user = new UserAccount();
        user.setFirstName("Juan");
        user.setLastName("Pérez");
        user.setEmail("juan@test.com");
        user.setIsActive(true);

        assertNotNull(user);
        assertEquals("Juan", user.getFirstName());
        assertEquals("juan@test.com", user.getEmail());
        assertTrue(user.getIsActive());
    }

    @Test
    void testRoleAssignment() {
        // Prueba Unitaria (TDD) para verificar la asignación de roles
        UserAccount user = new UserAccount();
        Role adminRole = new Role();
        adminRole.setName("ADMINISTRADOR");

        user.getRoles().add(adminRole);

        assertFalse(user.getRoles().isEmpty());
        assertEquals(1, user.getRoles().size());
        assertEquals("ADMINISTRADOR", user.getRoles().iterator().next().getName());
    }

    @Test
    void testCompanyEntityCreation() {
        // Prueba Unitaria (TDD) para verificar la creación de la entidad Company
        com.esendi.deriva.model.Company company = new com.esendi.deriva.model.Company();
        company.setTaxId("20123456789");
        company.setLegalName("Empresa Test S.A.");

        assertNotNull(company);
        assertEquals("20123456789", company.getTaxId());

    }

    @Test 
        void testDepartmentEntity() {
            com.esendi.deriva.model.Department dept = new com.esendi.deriva.model.Department();
            dept.setName("Soporte IT");
            dept.setDescription("Área de sistemas");

            assertEquals("Soporte IT", dept.getName());
            assertNotNull(dept.getDescription());   
        }
}