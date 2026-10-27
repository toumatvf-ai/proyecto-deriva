package com.esendi.deriva.controllers;

import com.esendi.deriva.model.Incident;
import com.esendi.deriva.security.CustomUserDetails;
import com.esendi.deriva.service.CatalogService;
import com.esendi.deriva.service.IncidentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final IncidentService incidentService;
    private final CatalogService catalogService;

    public DashboardController(IncidentService incidentService, CatalogService catalogService) {
        this.incidentService = incidentService;
        this.catalogService = catalogService;
    }

    @GetMapping("/client")
    public String clientDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Incident> incidents = incidentService.getIncidentsByClient(userDetails.getUserId());
        model.addAttribute("incidents", incidents);
        return "dashboard/client_dashboard";
    }

    @GetMapping("/advisor")
    public String advisorDashboard(Model model) {
        List<Incident> pendingIncidents = incidentService.getPendingIncidents();
        model.addAttribute("incidents", pendingIncidents);
        return "dashboard/advisor_dashboard";
    }

    @GetMapping("/technician")
    public String technicianDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Incident> assignedIncidents = incidentService.getIncidentsByTechnician(userDetails.getUserId());
        model.addAttribute("incidents", assignedIncidents);

        Integer departmentId = userDetails.getDepartmentId();
        if (departmentId != null) {
            String departmentName = catalogService.getDepartmentById(departmentId).getName();
            model.addAttribute("departmentName", departmentName);
        } else {
            model.addAttribute("errorMsg", "Atención: Tu usuario no tiene un área asignada en el sistema. Contacta al administrador.");
        }

        return "dashboard/technician_dashboard";
    }
}