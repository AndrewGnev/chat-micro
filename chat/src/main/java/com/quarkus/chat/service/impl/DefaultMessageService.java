package com.quarkus.chat.service.impl;

import com.quarkus.chat.model.Message;
import com.quarkus.chat.model.Room;
import com.quarkus.chat.repository.MessageRepository;
import com.quarkus.chat.service.MessageService;
import com.quarkus.chat.service.RoomService;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@ApplicationScoped
@RequiredArgsConstructor
public class DefaultMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final RoomService roomService;

    @Override
    @CacheInvalidate(cacheName = "messages")
    public void registerMessage(String username, @CacheKey Long roomId, String content) {
        final Room room = roomService.getById(roomId);
        final Message newMessage = Message.builder()
                .sender(username)
                .room(room)
                .content(content)
                .build();
        messageRepository.persistAndFlush(newMessage);
    }

    @Override
    @CacheResult(cacheName = "messages")
    public List<Message> loadMessages(@CacheKey Long roomId) {
        return roomService.getById(roomId).getMessages().stream()
                .sorted(Comparator.comparing(Message::getMessageId))
                .collect(Collectors.toList());
    }
}
