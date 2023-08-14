package com.quarkus.chat.service.impl;

import com.quarkus.chat.repository.RoomRepository;
import com.quarkus.chat.service.RoomService;
import com.quarkus.chat.model.Room;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Transactional
@ApplicationScoped
@RequiredArgsConstructor
public class DefaultRoomService implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    public long addRoom(String username) {
        final Room newRoom = Room.builder()
                .members(Set.of(username))
                .build();
        roomRepository.persistAndFlush(newRoom);
        return newRoom.getRoomId();
    }

    @Override
    public Room getById(long id) {
        return roomRepository.findByIdOptional(id)
                .orElseThrow(() -> new EntityNotFoundException("Room with id " + id + " not found"));
    }

    @Override
    public void deleteRoom(long roomId) {
        roomRepository.deleteById(roomId);
    }

    @Override
    public void joinRoom(long roomId, String username) {
        getById(roomId).getMembers().add(username);
    }

    @Override
    public void leaveRoom(long roomId, String username) {
        final Room dbRoom = getById(roomId);
        dbRoom.getMembers().remove(username);
        if (dbRoom.getMembers().isEmpty()) {
            roomRepository.deleteById(roomId);
        }
    }
}
