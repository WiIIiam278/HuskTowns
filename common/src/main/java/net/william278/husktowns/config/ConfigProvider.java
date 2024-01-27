package net.william278.husktowns.config;

import org.jetbrains.annotations.NotNull;

public interface ConfigProvider {

    @NotNull
    Settings getSettings();

    void setSettings(@NotNull Settings settings);

    @NotNull
    Locales getLocales();

    void setLocales(@NotNull Locales locales);

    @NotNull
    Roles getRoles();

    void setRoles(@NotNull Roles roles);

    @NotNull
    RulePresets getRulePresets();

    void setRulePresets(@NotNull RulePresets rulePresets);

    @NotNull
    Flags getFlags();

    void setFlags(@NotNull Flags flags);

    @NotNull
    Levels getLevels();

    void setLevels(@NotNull Levels levels);

    @NotNull
    String getServerName();

    void setServer(@NotNull Server server);

}
