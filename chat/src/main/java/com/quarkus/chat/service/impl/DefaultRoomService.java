package com.quarkus.chat.service.impl;

import com.quarkus.chat.service.RoomService;
import com.quarkus.chat.model.Room;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.Set;

@ApplicationScoped
@RequiredArgsConstructor
public class DefaultRoomService implements RoomService {
    @Override
    @Transactional
    public long addRoom(String username) {
        final Room newRoom = Room.builder()
                .members(Set.of(username))
                .build();
        Room.persistRoom(newRoom);
        Room.flush();
        return newRoom.getRoomId();
    }

    @Override
    @Transactional
    public Room getById(long id) {
        return Room.findById(id);
    }

    @Override
    @Transactional
    public void deleteRoom(long roomId) {
        Room.deleteById(roomId);
    }

    @Override
    @Transactional
    public void joinRoom(long roomId, String username) {
        getById(roomId).getMembers().add(username);
    }

    @Override
    @Transactional
    public void leaveRoom(long roomId, String username) {
        final Room dbRoom = Room.findById(roomId);
        dbRoom.getMembers().remove(username);
        if (dbRoom.getMembers().isEmpty()) {
            Room.deleteById(roomId);
        }
    }
}
