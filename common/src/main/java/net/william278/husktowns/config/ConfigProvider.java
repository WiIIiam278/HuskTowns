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

package net.william278.husktowns.config;

import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurationStore;
import de.exlll.configlib.YamlConfigurations;
import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public interface ConfigProvider {

    @NotNull
    YamlConfigurationProperties.Builder<?> YAML_CONFIGURATION_PROPERTIES = YamlConfigurationProperties.newBuilder()
        .charset(StandardCharsets.UTF_8)
        .setNameFormatter(NameFormatters.LOWER_UNDERSCORE);


    default void loadConfig() {
        loadSettings();
        loadLocales();
        loadRoles();
        loadRulePresets();
        loadFlags();
        loadLevels();
        loadServer();
    }

    @NotNull
    Settings getSettings();

    void setSettings(@NotNull Settings settings);

    default void loadSettings() {
        setSettings(YamlConfigurations.update(
            getConfigDirectory().resolve("config.yml"),
            Settings.class,
            YAML_CONFIGURATION_PROPERTIES.header(Settings.CONFIG_HEADER).build()
        ));
    }

    @NotNull
    Locales getLocales();

    void setLocales(@NotNull Locales locales);

    /**
     * Load the locales from the config file
     *
     * @since 1.0
     */
    default void loadLocales() {
        final YamlConfigurationStore<Locales> store = new YamlConfigurationStore<>(
            Locales.class, YAML_CONFIGURATION_PROPERTIES.header(Locales.CONFIG_HEADER).build()
        );
        // Read existing locales if present
        final Path path = getConfigDirectory().resolve(String.format("messages-%s.yml", getSettings().getLanguage()));
        if (Files.exists(path)) {
            setLocales(store.load(path));
            return;
        }

        // Otherwise, save and read the default locales
        try (InputStream input = getResource(String.format("locales/%s.yml", getSettings().getLanguage()))) {
            final Locales locales = store.read(input);
            store.save(locales, path);
            setLocales(locales);
        } catch (Throwable e) {
            getPlugin().log(Level.SEVERE, "An error occurred loading the locales (invalid lang code?)", e);
        }
    }

    @NotNull
    Roles getRoles();

    void setRoles(@NotNull Roles roles);

    default void loadRoles() {
        setRoles(YamlConfigurations.update(
            getConfigDirectory().resolve("roles.yml"),
            Roles.class,
            YAML_CONFIGURATION_PROPERTIES.header(Roles.CONFIG_HEADER).build()
        ));
    }

    @NotNull
    RulePresets getRulePresets();

    void setRulePresets(@NotNull RulePresets rulePresets);

    default void loadRulePresets() {
        setRulePresets(YamlConfigurations.update(
            getConfigDirectory().resolve("rules.yml"),
            RulePresets.class,
            YAML_CONFIGURATION_PROPERTIES.header(RulePresets.CONFIG_HEADER).build()
        ));
    }

    @NotNull
    Flags getFlags();

    void setFlags(@NotNull Flags flags);

    default void loadFlags() {
        setFlags(YamlConfigurations.update(
            getConfigDirectory().resolve("flags.yml"),
            Flags.class,
            YAML_CONFIGURATION_PROPERTIES.header(Flags.CONFIG_HEADER).build()
        ));
    }

    @NotNull
    Levels getLevels();

    void setLevels(@NotNull Levels levels);

    default void loadLevels() {
        setLevels(YamlConfigurations.update(
            getConfigDirectory().resolve("levels.yml"),
            Levels.class,
            YAML_CONFIGURATION_PROPERTIES.header(Levels.CONFIG_HEADER).build()
        ));
    }

    @NotNull
    String getServerName();

    void setServerName(@NotNull Server server);

    default void loadServer() {
        if (getSettings().getCrossServer().isEnabled()) {
            setServerName(YamlConfigurations.update(
                getConfigDirectory().resolve("server.yml"),
                Server.class,
                YAML_CONFIGURATION_PROPERTIES.header(Server.CONFIG_HEADER).build()
            ));
        }
    }

    /**
     * Get a plugin resource
     *
     * @param name The name of the resource
     * @return the resource, if found
     * @since 1.0
     */
    InputStream getResource(@NotNull String name);

    /**
     * Get the plugin config directory
     *
     * @return the plugin config directory
     * @since 1.0
     */
    @NotNull
    Path getConfigDirectory();

    @NotNull
    HuskTowns getPlugin();

}
