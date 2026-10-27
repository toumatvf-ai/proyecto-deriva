package com.esendi.deriva.security;

import com.esendi.deriva.model.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private final UserAccount userAccount;

    public CustomUserDetails(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    // Métodos personalizados para obtener IDs sin consultar la BD de nuevo
    public Long getUserId() {
        return userAccount.getUserAccountId();
    }

    public Integer getDepartmentId() {
        return userAccount.getDepartment() != null ? userAccount.getDepartment().getDepartmentId() : null;
    }

    public String getFullName() {
        return userAccount.getFirstName() + " " + userAccount.getLastName();
    }

    // Métodos obligatorios de Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userAccount.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return userAccount.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return userAccount.getEmail(); // Usamos el correo como username
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return userAccount.getIsActive(); }
}