package com.quarkus.chat.service.impl;

import com.quarkus.chat.dto.RegistrationDto;
import com.quarkus.chat.model.Role;
import com.quarkus.chat.model.User;
import com.quarkus.chat.service.UserService;
import io.quarkus.elytron.security.common.BcryptUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.Set;

@ApplicationScoped
public class DefaultUserService implements UserService {

    @Override
    @Transactional
    public boolean add(RegistrationDto dto, Set<Role> roles) {
        try {
            User.persistUser(User.builder()
                    .username(dto.getUsername())
                    .password(BcryptUtil.bcryptHash(dto.getPassword()))
                    .roles(roles)
                    .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public User getByUsername(String username) {
        return User.findByUsername(username);
    }
}
