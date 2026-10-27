package com.esendi.deriva.service;

import com.esendi.deriva.model.UserAccount;

public interface UserService {
    UserAccount getUserByEmail(String email);
}