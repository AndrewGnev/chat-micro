package com.quarkus.chat.service;

import com.quarkus.chat.dto.RegistrationDto;
import com.quarkus.chat.model.Role;
import com.quarkus.chat.model.User;

import java.util.Set;

public interface UserService {
    boolean add(RegistrationDto dto, Set<Role> roles);
    User getByUsername(String username);
}
