package com.quarkus.chat.jwt;

import com.quarkus.chat.model.Role;
import com.quarkus.chat.model.User;
import io.smallrye.jwt.build.Jwt;

import java.util.stream.Collectors;

public class JwtGenerator {

    public static String generateToken(User user) {
        return Jwt
                .upn(user.getUsername())
                .groups(
                        user.getRoles().stream()
                                .map(Role::name)
                                .collect(Collectors.toSet())
                )
                .sign();
    }
}
