package com.quarkus.chat.controller;

import com.quarkus.chat.dto.RegistrationDto;
import com.quarkus.chat.model.Role;
import com.quarkus.chat.service.UserService;
import lombok.RequiredArgsConstructor;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @POST
    @PermitAll
    public boolean registerUser(RegistrationDto registrationDto) {
        if (registrationDto.getPassword().equals(registrationDto.getRepeatedPassword()))
            return userService.add(registrationDto, Set.of(Role.USER));
        return false;
    }

    @POST
    @Path("/admin")
    @RolesAllowed("ADMIN")
    public boolean registerAdmin(RegistrationDto registrationDto) {
        if (registrationDto.getPassword().equals(registrationDto.getRepeatedPassword()))
            return userService.add(registrationDto, Set.of(Role.USER, Role.ADMIN));
        return false;
    }
}
