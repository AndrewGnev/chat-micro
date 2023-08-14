package com.quarkus.chat.controller;

import com.quarkus.chat.service.RoomService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import lombok.RequiredArgsConstructor;

@Path("/room")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @POST
    @RolesAllowed("USER")
    public long newRoom(@Context SecurityContext securityContext) {
        return roomService.addRoom(securityContext.getUserPrincipal().getName());
    }
}
