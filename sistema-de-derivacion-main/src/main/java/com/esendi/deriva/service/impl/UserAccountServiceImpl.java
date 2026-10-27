package com.esendi.deriva.service.impl;

import com.esendi.deriva.exception.ResourceNotFoundException;
import com.esendi.deriva.model.Company;
import com.esendi.deriva.model.Role;
import com.esendi.deriva.model.UserAccount;
import com.esendi.deriva.repository.RoleRepository;
import com.esendi.deriva.repository.UserAccountRepository;
import com.esendi.deriva.service.UserAccountService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountServiceImpl(
        UserAccountRepository userAccountRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void registerClientUser(String firstName, String lastName, String email, String rawPassword, String phone) {
        if (userAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }

        Role clientRole = roleRepository.findByName("CLIENTE")
                .orElseThrow(() -> new ResourceNotFoundException("Rol CLIENTE no encontrado en la base de datos."));
        
        UserAccount newUser = new UserAccount();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordEncoder.encode(rawPassword));
        newUser.setPhoneNumber(phone);
        newUser.setIsActive(true);

        newUser.getRoles().add(clientRole);
        userAccountRepository.save(newUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Company> getClientCompanies(Long clientId) {
        UserAccount user = userAccountRepository.findWithCompaniesByUserAccountId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return user.getCompanies();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAccount> getTechnicians() {
        return userAccountRepository.findByRoles_Name("TECNICO");
    }
}