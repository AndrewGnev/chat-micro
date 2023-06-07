package com.quarkus.chat.service.impl;

import com.quarkus.chat.model.Message;
import com.quarkus.chat.model.Room;
import com.quarkus.chat.service.MessageService;
import com.quarkus.chat.service.RoomService;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor
public class DefaultMessageService implements MessageService {
    private final RoomService roomService;

    @Override
    @Transactional
    public void registerMessage(String username, long roomId, String content) {
        final Room room = roomService.getById(roomId);
        final Message newMessage = Message.builder()
                .sender(username)
                .room(room)
                .content(content)
                .build();
        Message.persistMessage(newMessage);
    }

    @Override
    @Transactional
    public Collection<Message> loadMessages(long roomId) {
        return roomService.getById(roomId).getMessages().stream()
                .sorted(Comparator.comparing(Message::getMessageId))
                .collect(Collectors.toList());
    }
}
