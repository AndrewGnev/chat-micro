package com.quarkus.chat.controller;

import com.quarkus.chat.dto.AuthDto;
import com.quarkus.chat.service.AuthService;
import lombok.RequiredArgsConstructor;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @POST
    @PermitAll
    public String login(AuthDto dto) {
        return authService.login(dto);
    }
}
