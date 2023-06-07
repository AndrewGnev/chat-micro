package com.quarkus.chat.ws;

import com.quarkus.chat.model.Message;
import com.quarkus.chat.redis.RedisClient;
import com.quarkus.chat.redis.RedisPublisher;
import com.quarkus.chat.redis.api.ChatMessage;
import com.quarkus.chat.service.MessageService;
import com.quarkus.chat.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ServerEndpoint("/chat/{room_id}")
@ApplicationScoped
@RequiredArgsConstructor
public class ChatSocket {
    private final Map<Long, Set<Session>> roomsSessions = new ConcurrentHashMap<>();
    private final RoomService roomService;
    private final MessageService messageService;
    private final RedisClient redisClient;
    private final RedisPublisher redisPublisher;

    @ConfigProperty(name = "app.name")
    String instanceName;

    @OnOpen
    @RolesAllowed("USER")
    public void onOpen(Session session, @PathParam("room_id") long roomId) {
        CompletableFuture.runAsync(() -> {
            roomsSessions.merge(roomId, new HashSet<>(Set.of(session)), (existingSessions, newSessions) -> {
                existingSessions.addAll(newSessions);
                return existingSessions;
            });
            broadcastPreviousMessages(messageService.loadMessages(roomId), session);
            syncWithRedis(session, roomId);
            roomService.joinRoom(roomId, session.getUserPrincipal().getName());
        }).whenComplete((__, e) -> {
            if (e == null) {
                broadcast("User " + session.getUserPrincipal().getName() + " joined", roomId);
            } else {
                broadcast(e.getMessage(), roomId);
            }
        });
    }

    @OnClose
    public void onClose(Session session, @PathParam("room_id") long roomId) {
        CompletableFuture.runAsync(() -> {
            roomsSessions.get(roomId).remove(session);
            syncWithRedis(session, roomId);
            roomService.leaveRoom(roomId, session.getUserPrincipal().getName());
        }).whenComplete((__, e) -> {
            if (e == null) {
                broadcast("User " + session.getUserPrincipal().getName() + " left", roomId);
            } else {
                broadcast(e.getMessage(), roomId);
            }
        });
    }

    @OnError
    public void onError(Session session, @PathParam("room_id") long roomId, Throwable throwable) {
        CompletableFuture.runAsync(() -> {
            roomsSessions.get(roomId).remove(session);
            syncWithRedis(session, roomId);
            roomService.leaveRoom(roomId, session.getUserPrincipal().getName());
        }).whenComplete((__, e) -> {
            if (e == null) {
                broadcast("User " + session.getUserPrincipal().getName() + " left on error: " + throwable, roomId);
            } else {
                broadcast(e.getMessage(), roomId);
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
            syncWithRedis(session, roomId);
            redisPublisher.publish(session.getId(), roomId, message);
        }).whenComplete((__, e) -> {
            if (e == null) {
                broadcast(">> " + session.getUserPrincipal().getName() + ": " + message, roomId);
            } else {
                broadcast(e.getMessage(), roomId);
            }
        });
    }

    public void broadcastForeignMessage(ChatMessage message) {
        if (isForeign(message)) {
            broadcast(message.getContent(), message.getRoomId());
        }
    }

    private boolean isForeign(ChatMessage message) {
        return !Set.of(redisClient.getInstanceSessions(instanceName)).contains(message.getSession());

//        return !roomsSessions.values().stream()
//                .reduce(new HashSet<>(), (acc, set) -> {
//                    acc.addAll(set);
//                    return acc;
//                }).stream()
//                .map(Session::getId)
//                .collect(Collectors.toSet())
//                .contains(message.getSession());
    }

    private void broadcast(String message, long roomId) {
        roomsSessions.get(roomId).forEach(session -> {
            session.getAsyncRemote().sendObject(ProcessHandle.current().pid() + " " + message, result ->  {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }

    private void broadcastPreviousMessages(Collection<Message> messages, Session session) {
        messages.forEach(message -> {
            session.getAsyncRemote().sendObject( ProcessHandle.current().pid() + " >> " + message.getSender() + ": " + message.getContent(), result -> {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }

    private void syncWithRedis(Session session, long roomId) {
        final String instance = redisClient.getSessionInstance(session.getId());
        if (!instanceName.equals(instance)) {
            if (instance != null) {
                final String[] instanceSessions = Optional
                        .ofNullable(redisClient.getInstanceSessions(instance))
                        .orElse(new String[0]);
                redisClient.setInstanceSessions(
                        instance,
                        Arrays.stream(instanceSessions)
                                .filter(instanceSession -> !instanceSession.equals(session.getId()))
                                .toArray(String[]::new)
                );
            }

        }

        redisClient.setSessionInstance(session.getId(), instanceName);
        redisClient.setInstanceSessions(
                instanceName,
                roomsSessions.values().stream()
                        .reduce(new HashSet<>(), (acc, set) -> {
                            acc.addAll(set);
                            return acc;
                        }).stream()
                        .map(Session::getId)
                        .collect(Collectors.toSet())
                        .toArray(String[]::new)
        );

        roomsSessions.computeIfPresent(roomId, (room, oldSessions) -> {
            final Set<String> sessionsStoredInRedis = Set.of(redisClient.getInstanceSessions(instanceName));
            return oldSessions.stream()
                    .filter(oldSession -> sessionsStoredInRedis.contains(oldSession.getId()))
                    .collect(Collectors.toSet());
        });
    }

}