package com.quarkus.chat.controller;

import com.quarkus.chat.service.RoomService;
import lombok.RequiredArgsConstructor;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

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
