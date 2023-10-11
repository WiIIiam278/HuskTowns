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

package net.william278.husktowns.gui;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.gui.census.CensusGui;
import net.william278.husktowns.gui.deeds.DeedsGui;
import net.william278.husktowns.gui.list.TownListGui;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;


public class BukkitGUIManager implements GUIManager {

    private final BukkitHuskTowns plugin;

    private final GuiSettings guiSettings;

    public BukkitGUIManager(BukkitHuskTowns plugin) {
        this.guiSettings = loadSettings(plugin);
        GuiSettings.setInstance(guiSettings);
        this.plugin = plugin;
    }

    public GuiSettings loadSettings(BukkitHuskTowns plugin) {
        final File advancementsFile = new File(plugin.getDataFolder(), "guisettings.json");
        if (!advancementsFile.exists()) {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(advancementsFile), StandardCharsets.UTF_8)) {
                plugin.getGsonBuilder().setPrettyPrinting().create().toJson(new GuiSettings(), writer);
            } catch (Exception e) {
                plugin.log(Level.SEVERE, "Failed to write default gui settings: " + e.getMessage(), e);
            }
        }

        // Read advancements from file
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(advancementsFile), StandardCharsets.UTF_8)) {
            return plugin.getGson().fromJson(reader, GuiSettings.class);
        } catch (Exception e) {
            plugin.log(Level.SEVERE, "Failed to read advancements: " + e.getMessage(), e);
        }
        return null;
    }


    @Override
    public void openTownGUI(OnlineUser executor, Town town) {
        //TODO: Implement
    }

    @Override
    public void openTownListGUI(OnlineUser executor, Town town) {
        TownListGui gui = new TownListGui(plugin);

        plugin.getScheduler().globalRegionalScheduler()
                .run(() -> gui.open(plugin.getServer().getPlayer(executor.getUuid())));
    }

    @Override
    public void openDeedsGUI(OnlineUser executor, Town town) {
        DeedsGui dg = new DeedsGui(executor, town, plugin);
        plugin.getScheduler().globalRegionalScheduler()
                .run(() ->
                        dg.openWindow(plugin.getServer().getPlayer(executor.getUuid())));


    }

    @Override
    public void openCensusGUI(OnlineUser executor, Town town) {

        CensusGui censusGui = new CensusGui(town.getMembers().entrySet().stream()
                .map(entry -> {
                    Optional<Role> role = plugin.getRoles().fromWeight(entry.getValue());
                    return role.flatMap(value ->
                                    plugin.getDatabase().getUser(entry.getKey())
                                            .map(user ->
                                                    new Member(user.user(), town, value)))
                            .orElse(null);
                })
                .toList());
        plugin.getScheduler().globalRegionalScheduler()
                .run(() ->
                        censusGui.open(plugin.getServer().getPlayer(executor.getUuid())));
    }
    public IGuiSettings getGuiSettings() {
        return guiSettings;
    }

}
