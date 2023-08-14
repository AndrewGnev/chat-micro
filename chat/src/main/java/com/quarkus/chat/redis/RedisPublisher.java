package com.quarkus.chat.redis;

import com.quarkus.chat.redis.api.ChatMessage;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RedisPublisher {

    private final ValueCommands<String, ChatMessage> commands;
    private final PubSubCommands<ChatMessage> pub;

    @ConfigProperty(name = "app.redis.chat-messages.channel-name")
    String channelName;

    public RedisPublisher(RedisDataSource ds) {
        commands = ds.value(ChatMessage.class);
        pub = ds.pubsub(ChatMessage.class);
    }

    public void publish(String sessionId, long roomId, String content, String sender) {
        pub.publish(
                channelName + roomId + sessionId,
                ChatMessage.builder()
                        .session(sessionId)
                        .roomId(roomId)
                        .content(content)
                        .sender(sender)
                        .build()
        );
    }
}
