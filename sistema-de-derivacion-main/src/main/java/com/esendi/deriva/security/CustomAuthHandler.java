package com.esendi.deriva.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        @SuppressWarnings("null")
        long validRolesCount = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(role -> role.equals("CLIENTE") || role.equals("ASESOR") || role.equals("TECNICO") || role.equals("ADMINISTRADOR"))
                .distinct()
                .count();
        
        if (validRolesCount > 1) {
            response.sendRedirect("/role-selection");
            return;
        }

        String redirectUrl = "/";
        String role = authorities.iterator().next().getAuthority();

        if (role.equals("CLIENTE")) {
            redirectUrl = "/dashboard/client";
        } else if (role.equals("ASESOR")) {
            redirectUrl = "/dashboard/advisor";
        } else if (role.equals("TECNICO")) {
            redirectUrl = "/dashboard/technician";
        } else if (role.equals("ADMINISTRADOR")) {
            redirectUrl = "/admin/dashboard";
        }
        
        response.sendRedirect(redirectUrl);
    }
}