package com.quarkus.chat.ws;

import com.quarkus.chat.model.Message;
import com.quarkus.chat.redis.RedisPublisher;
import com.quarkus.chat.redis.api.ChatMessage;
import com.quarkus.chat.service.MessageService;
import com.quarkus.chat.service.RoomService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint("/chat/{room_id}")
@ApplicationScoped
@RequiredArgsConstructor
public class ChatSocket {
    private final Map<Long, Set<Session>> roomsSessions = new ConcurrentHashMap<>();
    private final RoomService roomService;
    private final MessageService messageService;
    private final RedisPublisher redisPublisher;

    @OnOpen
    @RolesAllowed("USER")
    public void onOpen(Session session, @PathParam("room_id") long roomId) {
        CompletableFuture.runAsync(() -> {
            roomsSessions.merge(roomId, new HashSet<>(Set.of(session)), (existingSessions, newSessions) -> {
                existingSessions.addAll(newSessions);
                return existingSessions;
            });
            broadcastPreviousMessages(messageService.loadMessages(roomId), session);
            roomService.joinRoom(roomId, session.getUserPrincipal().getName());
        }).whenComplete((__, e) -> {
            if (e == null) {
                broadcastForRoom("User " + session.getUserPrincipal().getName() + " joined", roomId);
            } else {
                broadcastMessage(session, e.getMessage());
            }
        });
    }

    @OnClose
    public void onClose(Session session, @PathParam("room_id") long roomId) {
        CompletableFuture.runAsync(() -> {
            roomsSessions.get(roomId).remove(session);
            roomService.leaveRoom(roomId, session.getUserPrincipal().getName());
        }).whenComplete((__, e) -> {
            if (e == null) {
                broadcastForRoom("User " + session.getUserPrincipal().getName() + " left", roomId);
            } else {
                broadcastMessage(session, e.getMessage());
            }
        });
    }

    @OnError
    public void onError(Session session, @PathParam("room_id") long roomId, Throwable throwable) {
        CompletableFuture.runAsync(() -> {
            roomsSessions.get(roomId).remove(session);
            roomService.leaveRoom(roomId, session.getUserPrincipal().getName());
        }).whenComplete((__, e) -> {
            if (e == null) {
                broadcastForRoom("User " + session.getUserPrincipal().getName() + " left on error: " + throwable, roomId);
            } else {
                broadcastMessage(session, e.getMessage());
            }
        });
    }

    @OnMessage
    @RolesAllowed("USER")
    public void onMessage(Session session, String message, @PathParam("room_id") long roomId) {
        CompletableFuture.runAsync(() -> {
            messageService.registerMessage(
                    session.getUserPrincipal().getName(),
                    roomId,
                    message
            );
            redisPublisher.publish(session.getId(), roomId, message, session.getUserPrincipal().getName());
        }).whenComplete((__, e) -> {
            if (e == null) {
                broadcastMessage(session, session.getUserPrincipal().getName() + ": " + message);
            } else {
                broadcastMessage(session, e.getMessage());
            }
        });
    }

    public void broadcastMessage(ChatMessage message) {
        roomsSessions.get(message.getRoomId()).stream()
                .filter(session -> !session.getId().equals(message.getSession()))
                .forEach(session -> broadcastMessage(session, message.getSender() + ": " + message.getContent()));
    }

    private void broadcastForRoom(String message, long roomId) {
        roomsSessions.get(roomId)
                .forEach(session -> broadcastMessage(session, message));
    }

    private void broadcastPreviousMessages(Collection<Message> messages, Session session) {
        messages.forEach(
                message -> broadcastMessage(session, message.getSender() + ": " + message.getContent())
        );
    }

    private void broadcastMessage(Session session, String content) {
        session.getAsyncRemote().sendObject(content, result -> {
            if (result.getException() != null) {
                log.error("Unable to send message", result.getException());
            }
        });
    }
}