/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.network;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.logging.Level;

/**
 * Redis message broker implementation
 */
public class RedisBroker extends PluginMessageBroker {
    private JedisPool jedisPool;

    public RedisBroker(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @Override
    public void initialize() throws RuntimeException {
        super.initialize();

        final String password = plugin.getSettings().getRedisPassword();
        final String host = plugin.getSettings().getRedisHost();
        final int port = plugin.getSettings().getRedisPort();
        final boolean useSSL = plugin.getSettings().useRedisSsl();

        this.jedisPool = password.isEmpty() ? new JedisPool(new JedisPoolConfig(), host, port, 0, useSSL)
                : new JedisPool(new JedisPoolConfig(), host, port, 0, password, useSSL);

        new Thread(getSubscriber(), plugin.getKey("redis_subscriber").toString()).start();

        plugin.log(Level.INFO, "Initialized Redis connection pool");
    }

    @NotNull
    private Runnable getSubscriber() {
        return () -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(@NotNull String channel, @NotNull String encodedMessage) {
                        if (!channel.equals(getSubChannelId())) {
                            return;
                        }

                        final Message message = plugin.getMessageFromJson(encodedMessage);
                        if (message.getTargetType() == Message.TargetType.PLAYER) {
                            plugin.getOnlineUsers().stream()
                                    .filter(online -> online.getUsername().equalsIgnoreCase(message.getTarget()))
                                    .findFirst()
                                    .ifPresent(receiver -> handle(receiver, message));
                            return;
                        }
                        handle(plugin.getOnlineUsers().stream().findAny().orElse(null), message);
                    }
                }, getSubChannelId());
            }
        };
    }

    @Override
    protected void send(@NotNull Message message, @NotNull OnlineUser sender) {
        plugin.runAsync(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(getSubChannelId(), plugin.getGson().toJson(message));
            }
        });
    }

    @Override
    public void close() {
        super.close();
        if (jedisPool != null) {
            jedisPool.close();
        }
    }

}
