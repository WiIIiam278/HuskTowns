/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
 */

package net.william278.husktowns.config;

import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import org.jetbrains.annotations.NotNull;

@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃  HuskTowns Server ID config  ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ This file should contain the ID of this server as defined in your proxy config.
        ┣╸ If you join it using /server alpha, then set it to 'alpha' (case-sensitive)
        ┗╸ You only need to touch this if you're using cross-server mode.""")
public class Server {

    @YamlKey("name")
    private String serverName = "server";

    private Server(@NotNull String serverName) {
        this.serverName = serverName;
    }

    @SuppressWarnings("unused")
    private Server() {
    }

    @NotNull
    public static Server of(@NotNull String serverName) {
        return new Server(serverName);
    }

    @NotNull
    public String getName() {
        return serverName;
    }

}
