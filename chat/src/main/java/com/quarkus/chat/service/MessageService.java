package com.quarkus.chat.service;

import com.quarkus.chat.model.Message;

import java.util.Collection;

public interface MessageService {
    void registerMessage(String username, long roomId, String content);

    Collection<Message> loadMessages(long roomId);
}
