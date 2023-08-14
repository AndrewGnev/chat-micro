package com.quarkus.chat.service;

import com.quarkus.chat.model.Message;

import java.util.Collection;
import java.util.List;

public interface MessageService {
    void registerMessage(String username, Long roomId, String content);

    List<Message> loadMessages(Long roomId);
}
