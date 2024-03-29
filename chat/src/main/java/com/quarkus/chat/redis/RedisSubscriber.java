package com.quarkus.chat.redis;

import com.quarkus.chat.redis.api.ChatMessage;
import com.quarkus.chat.ws.ChatSocket;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ApplicationScoped
@Startup
public class RedisSubscriber implements Consumer<ChatMessage> {

    private final PubSubCommands<ChatMessage> pub;
    private final ChatSocket chatSocket;
    private PubSubCommands.RedisSubscriber subscriber;

    @ConfigProperty(name = "app.redis.chat-messages.channel-name")
    String channelName;

    public RedisSubscriber(RedisDataSource ds, ChatSocket chatSocket) {
        this.pub = ds.pubsub(ChatMessage.class);
        this.chatSocket = chatSocket;
    }

    @PostConstruct
    public void init() {
        this.subscriber = pub.subscribeToPattern(channelName + "*", this);
    }

    @Override
    public void accept(ChatMessage message) {
        CompletableFuture.runAsync(() -> chatSocket.broadcastMessage(message));
    }

    @PreDestroy
    public void terminate() {
        subscriber.unsubscribe();
    }
}
