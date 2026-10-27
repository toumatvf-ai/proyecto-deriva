package com.esendi.deriva.service;

import com.esendi.deriva.model.Role;
import com.esendi.deriva.model.UserAccount;
import java.io.IOException;
import java.util.List;

public interface AdminService {
    /**
     * Exporta todas las incidencias registradas a un archivo Excel (.xlsx).
     * Utiliza la librería Apache POI
     */
    byte[] exportIncidentsToExcel() throws IOException;
    
    List<UserAccount> getAllUsers();
    
    UserAccount getUserById(Long userId);
    
    List<Role> getAllRoles();
    
    void saveInternalUser(Long userId, String firstName, String lastName, String email, String password, String phone, Integer departmentId, List<Integer> roleIds);
    
    void toggleUserStatus(Long userId);
}