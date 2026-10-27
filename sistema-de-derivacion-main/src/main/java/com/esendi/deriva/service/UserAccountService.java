package com.esendi.deriva.service;

import com.esendi.deriva.model.Company;
import com.esendi.deriva.model.UserAccount;
import java.util.List;
import java.util.Set;

public interface UserAccountService {
    void registerClientUser(String firstName, String lastName, String email, String rawPassword, String phone);
    Set<Company> getClientCompanies(Long clientId);
    List<UserAccount> getTechnicians();
}