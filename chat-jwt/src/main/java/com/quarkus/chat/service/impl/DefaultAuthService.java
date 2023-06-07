package com.quarkus.chat.service.impl;

import com.quarkus.chat.service.AuthService;
import com.quarkus.chat.dto.AuthDto;
import com.quarkus.chat.jwt.JwtGenerator;
import com.quarkus.chat.model.User;
import com.quarkus.chat.service.UserService;
import io.quarkus.elytron.security.common.BcryptUtil;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RequiredArgsConstructor
public class DefaultAuthService implements AuthService {

    private final UserService userService;

    @Override
    public String login(AuthDto dto) {
        final User dbUser = userService.getByUsername(dto.getUsername());
        if (!BcryptUtil.matches(dto.getPassword(), dbUser.getPassword()))
            throw new RuntimeException();
        return JwtGenerator.generateToken(dbUser);
    }
}
