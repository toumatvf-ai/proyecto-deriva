package com.esendi.deriva.controllers;

import com.esendi.deriva.model.UserAccount;
import com.esendi.deriva.service.AdminService;
import com.esendi.deriva.service.CatalogService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final CatalogService catalogService;

    public AdminController(AdminService adminService, CatalogService catalogService) {
        this.adminService = adminService;
        this.catalogService = catalogService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "dashboard/admin_dashboard";
    }

    @GetMapping("/export/incidents")
    public ResponseEntity<byte[]> exportIncidents() throws IOException {
        byte[] excelContent = adminService.exportIncidentsToExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Incidencias.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelContent);
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        return "admin/user_list";
    }

    @GetMapping("/users/new")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new UserAccount());
        model.addAttribute("roles", adminService.getAllRoles());
        model.addAttribute("departments", catalogService.getAllDepartments());
        return "admin/user_form";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", adminService.getUserById(id));
        model.addAttribute("roles", adminService.getAllRoles());
        model.addAttribute("departments", catalogService.getAllDepartments());
        return "admin/user_form";
    }

    @PostMapping("/users/save")
    public String saveUser(
            @RequestParam(required = false) Long userAccountId,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) List<Integer> roleIds,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminService.saveInternalUser(userAccountId, firstName, lastName, email, password, phone, departmentId, roleIds);
            redirectAttributes.addFlashAttribute("successMsg", "Usuario guardado exitosamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            if (userAccountId != null) {
                return "redirect:/admin/users/edit/" + userAccountId;
            }
            return "redirect:/admin/users/new";
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/toggle/{id}")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.toggleUserStatus(id);
        redirectAttributes.addFlashAttribute("successMsg", "Estado del usuario actualizado.");
        return "redirect:/admin/users";
    }
}