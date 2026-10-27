package com.esendi.deriva.service.impl;

import com.esendi.deriva.exception.ResourceNotFoundException;
import com.esendi.deriva.model.Department;
import com.esendi.deriva.model.Incident;
import com.esendi.deriva.model.Role;
import com.esendi.deriva.model.UserAccount;
import com.esendi.deriva.repository.DepartmentRepository;
import com.esendi.deriva.repository.IncidentRepository;
import com.esendi.deriva.repository.RoleRepository;
import com.esendi.deriva.repository.UserAccountRepository;
import com.esendi.deriva.service.AdminService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminServiceImpl implements AdminService {

    private final IncidentRepository incidentRepository;
    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(
        IncidentRepository incidentRepository,
        UserAccountRepository userAccountRepository,
        RoleRepository roleRepository,
        DepartmentRepository departmentRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.incidentRepository = incidentRepository;
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public byte[] exportIncidentsToExcel() throws IOException {
        // Ordenar el reporte de Excel por fecha descendente (los más recientes primero)
        List<Incident> incidents = incidentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Incidencias");
            
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Código", "Prioridad", "Título", "Estado", "Cliente", "Empresa", "Área", "Técnico"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }
            
            int rowIdx = 1;
            for (Incident inc : incidents) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(inc.getCode() != null ? inc.getCode() : "N/A");
                row.createCell(1).setCellValue(inc.getPriority() != null ? inc.getPriority() : "N/A");
                row.createCell(2).setCellValue(inc.getTitle());
                row.createCell(3).setCellValue(inc.getIncidentStatus().getName());
                row.createCell(4).setCellValue(inc.getClient().getFirstName() + " " + inc.getClient().getLastName());
                row.createCell(5).setCellValue(inc.getCompany().getLegalName());
                row.createCell(6).setCellValue(inc.getDepartment() != null ? inc.getDepartment().getName() : "N/A");
                row.createCell(7).setCellValue(inc.getAssignedTo() != null ? inc.getAssignedTo().getFirstName() + " " + inc.getAssignedTo().getLastName() : "N/A");
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAccount> getAllUsers() {
        return userAccountRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public UserAccount getUserById(Long userId) {
        return userAccountRepository.findWithDetailsByUserAccountId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional
    public void saveInternalUser(Long userId, String firstName, String lastName, String email, String password, String phone, Integer departmentId, List<Integer> roleIds) {
        
        if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un rol para el usuario.");
        }

        UserAccount user;
        
        if (userId != null) {
            user = getUserById(userId);
            if (!user.getEmail().equals(email) && userAccountRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("El correo ya está en uso por otro usuario.");
            }
        } else {
            if (userAccountRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("El correo ya está registrado.");
            }
            user = new UserAccount();
            user.setIsActive(true);
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhoneNumber(phone);

        if (password != null && !password.trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(password));
        } else if (userId == null) {
            throw new IllegalArgumentException("La contraseña es obligatoria para usuarios nuevos.");
        }

        if (departmentId != null) {
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Área no encontrada"));
            user.setDepartment(dept);
        } else {
            user.setDepartment(null);
        }

        Set<Role> roles = new HashSet<>();
        for (Integer roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));
            roles.add(role);
        }
        user.setRoles(roles);

        userAccountRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId) {
        UserAccount user = getUserById(userId);
        user.setIsActive(!user.getIsActive());
        userAccountRepository.save(user);
    }
}