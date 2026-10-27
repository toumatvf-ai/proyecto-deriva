package com.esendi.deriva.controllers;

import com.esendi.deriva.model.Incident;
import com.esendi.deriva.model.IncidentHistory;
import com.esendi.deriva.repository.UserAccountRepository;
import com.esendi.deriva.security.CustomUserDetails;
import com.esendi.deriva.service.CatalogService;
import com.esendi.deriva.service.IncidentService;
import com.esendi.deriva.service.UserAccountService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/incident")
public class IncidentController {

    private final IncidentService incidentService;
    private final CatalogService catalogService;
    private final UserAccountService userAccountService;
    private final UserAccountRepository userAccountRepository;

    public IncidentController(
        IncidentService incidentService, 
        CatalogService catalogService, 
        UserAccountService userAccountService,
        UserAccountRepository userAccountRepository
    ) {
        this.incidentService = incidentService;
        this.catalogService = catalogService;
        this.userAccountService = userAccountService;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping("/new")
    public String showCreateForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("companies", userAccountService.getClientCompanies(userDetails.getUserId()));
        return "incident/create_form";
    }

    @PostMapping("/create")
    public String createIncident(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) Long companyId,
        @RequestParam(required = false) String companyRuc,
        @RequestParam(required = false) String companyName,
        @RequestParam String title,
        @RequestParam String description,
        @RequestParam(required = false) String photoUrl
    ) {
        incidentService.createIncident(userDetails.getUserId(), companyId, companyRuc, companyName, title, description, photoUrl);
        return "redirect:/dashboard/client";
    }

    @GetMapping("/{incidentId}")
    public String viewIncidentDetail(
        @PathVariable Long incidentId, 
        @AuthenticationPrincipal CustomUserDetails userDetails,
        Model model
    ) {
        Incident incident = incidentService.getIncidentById(incidentId);
        List<IncidentHistory> history = incidentService.getIncidentHistory(incidentId);
        
        model.addAttribute("incident", incident);
        model.addAttribute("history", history);
        model.addAttribute("departments", catalogService.getAllDepartments());
        model.addAttribute("technicians", userAccountRepository.findByRoles_Name("TECNICO"));
        
        @SuppressWarnings("null")
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        model.addAttribute("roles", roles); 
        
        return "incident/detail";
    }

    @PostMapping("/{incidentId}/route")
    public String routeIncident(
        @PathVariable Long incidentId,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam Integer departmentId,
        @RequestParam Long technicianId,
        @RequestParam String priority,
        @RequestParam String comment
    ) {
        incidentService.routeIncident(incidentId, userDetails.getUserId(), departmentId, technicianId, priority, comment);
        return "redirect:/dashboard/advisor";
    }

    @PostMapping("/{incidentId}/reject")
    public String rejectIncident(
        @PathVariable Long incidentId,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam String comment
    ) {
        incidentService.rejectIncident(incidentId, userDetails.getUserId(), comment);
        return "redirect:/dashboard/advisor";
    }

    @PostMapping("/{incidentId}/start")
    public String startProgress(
        @PathVariable Long incidentId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        incidentService.startIncidentProgress(incidentId, userDetails.getUserId());
        return "redirect:/dashboard/technician";
    }

    @PostMapping("/{incidentId}/resolve")
    public String resolveIncident(
        @PathVariable Long incidentId,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam String resolutionComment,
        @RequestParam(required = false) String evidenceUrl) {
        incidentService.resolveIncident(incidentId, userDetails.getUserId(), resolutionComment, evidenceUrl);
        return "redirect:/dashboard/technician";
    }
}