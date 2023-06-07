package com.quarkus.chat.redis.api;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChatMessage {
    String session;
    long roomId;
    String content;
}
