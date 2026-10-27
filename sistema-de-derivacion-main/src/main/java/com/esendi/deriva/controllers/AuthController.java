package com.esendi.deriva.controllers;

import com.esendi.deriva.security.CustomUserDetails;
import com.esendi.deriva.service.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserAccountService userAccountService;
    private final UserDetailsService userDetailsService;

    public AuthController(UserAccountService userAccountService, UserDetailsService userDetailsService) {
        this.userAccountService = userAccountService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/role-selection")
    public String roleSelection(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("roles", userDetails.getAuthorities());
        model.addAttribute("fullName", userDetails.getFullName());
        return "auth/role_selection";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "auth/register";
    }

    @PostMapping("/register/process")
    public String processRegistration(
        @RequestParam String firstName,
        @RequestParam String lastName,
        @RequestParam String email,
        @RequestParam String password,
        @RequestParam(required = false) String phone,
        HttpServletRequest request,
        HttpServletResponse response,
        RedirectAttributes redirectAttributes
    ) {
        try {
            // Registrar al usuario en la base de datos
            userAccountService.registerClientUser(firstName, lastName, email, password, phone);
            
            // AUTO-LOGIN (Compatible con Spring Security 6)
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            // Crear un nuevo contexto de seguridad y asignarle la autenticación
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            
            // Guardar explícitamente el contexto en la sesión HTTP para que sobreviva al redirect
            SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
            securityContextRepository.saveContext(context, request, response);
            
            // Redirigir directamente al dashboard del cliente
            return "redirect:/dashboard/client";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/register";
        }
    }
}