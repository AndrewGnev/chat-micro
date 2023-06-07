package com.quarkus.chat.service;

import com.quarkus.chat.model.Room;

public interface RoomService {
    long addRoom(String username);
    Room getById(long id);
    void deleteRoom(long roomId);
    void joinRoom(long roomId, String username);
    void leaveRoom(long roomId, String username);
}
