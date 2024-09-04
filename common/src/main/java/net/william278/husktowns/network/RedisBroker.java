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

import com.google.common.collect.Sets;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.*;
import redis.clients.jedis.util.Pool;

import java.util.Set;
import java.util.logging.Level;

/**
 * Redis message broker implementation
 */
public class RedisBroker extends PluginMessageBroker {
    private Pool<Jedis> jedisPool;

    public RedisBroker(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @Override
    public void initialize() throws RuntimeException {
        super.initialize();

        this.jedisPool = establishJedisPool();
        new Thread(getSubscriber(), plugin.getKey("redis_subscriber").toString()).start();

        plugin.log(Level.INFO, "Initialized Redis connection pool");
    }

    @NotNull
    private Pool<Jedis> establishJedisPool() {
        final Settings.CrossServerSettings.RedisSettings settings = plugin.getSettings().getCrossServer().getRedis();
        final Set<String> sentinelNodes = Sets.newHashSet(settings.getSentinel().getNodes());
        final Pool<Jedis> pool;
        if (sentinelNodes.isEmpty()) {
            pool = new JedisPool(
                new JedisPoolConfig(),
                settings.getHost(),
                settings.getPort(),
                0,
                settings.getPassword().isEmpty() ? null : settings.getPassword(),
                settings.isUseSsl()
            );
            plugin.log(Level.INFO, "Using Redis pool");
        } else {
            pool = new JedisSentinelPool(
                settings.getSentinel().getMasterName(),
                sentinelNodes,
                settings.getPassword().isEmpty() ? null : settings.getPassword(),
                settings.getSentinel().getPassword().isEmpty() ? null : settings.getSentinel().getPassword()
            );
            plugin.log(Level.INFO, "Using Redis Sentinel pool");
        }
        return pool;
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
                                .filter(online -> online.getName().equalsIgnoreCase(message.getTarget()))
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
