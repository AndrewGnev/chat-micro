package com.quarkus.chat.redis;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.ReactiveKeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

@Singleton
public class RedisClient {
    private final ReactiveKeyCommands<String> keys;
    private final ValueCommands<String, String[]> instancesSessions;
    private final ValueCommands<String, String> sessionsInstances;

    @ConfigProperty(name = "app.redis.instance.key-prefix")
    String instancesKeyPrefix;

    @ConfigProperty(name = "app.redis.session.key-prefix")
    String sessionsKeyPrefix;

    public RedisClient(RedisDataSource redisDS, ReactiveRedisDataSource reactiveRedisDS) {
        keys = reactiveRedisDS.key();
        instancesSessions = redisDS.value(String[].class);
        sessionsInstances = redisDS.value(String.class);
    }

    public List<String> getInstancesKeys() {
        try {
            return keys.keys(instancesKeyPrefix + "?*").subscribe().asCompletionStage().get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    public List<String> getSessionsKeys() {
        try {
            return keys.keys(sessionsKeyPrefix + "?*").subscribe().asCompletionStage().get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }

    public void setInstanceSessions(String instance, String[] sessions) {
        instancesSessions.set(instance, sessions);
    }

    public void setSessionInstance(String session, String instance) {
        sessionsInstances.set(session, instance);
    }

    public String getSessionInstance(String session) {
        return sessionsInstances.get(session);
    }

    public String[] getInstanceSessions(String instance) {
        return instancesSessions.get(instance);
    }

}
