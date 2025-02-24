package net.william278.husktowns.util;

import net.william278.cloplib.operation.OperationType;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.hook.Hook;
import net.william278.husktowns.hook.PluginHook;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.toilet.DumpOptions;
import net.william278.toilet.Toilet;
import net.william278.toilet.dump.DumpUser;
import net.william278.toilet.dump.PluginInfo;
import net.william278.toilet.dump.PluginStatus;
import net.william278.toilet.dump.ProjectMeta;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.william278.toilet.DumpOptions.builder;

public interface DumpProvider {

    @NotNull String BYTEBIN_URL = "https://bytebin.lucko.me";
    @NotNull String VIEWER_URL = "https://william278.net/dump";

    @NotNull
    Toilet getToilet();

    @NotNull
    @Blocking
    default String createDump(@NotNull CommandUser u) {
        return getToilet().dump(getPluginStatus(), u instanceof OnlineUser o
                ? new DumpUser(o.getName(), o.getUuid()) : null).toString();
    }

    @NotNull
    default DumpOptions getDumpOptions() {
        return builder()
                .bytebinUrl(BYTEBIN_URL)
                .viewerUrl(VIEWER_URL)
                .projectMeta(ProjectMeta.builder()
                        .id("husktowns")
                        .name("HuskTowns")
                        .version(getPlugin().getPluginVersion().toString())
                        .md5("unknown")
                        .author("William278")
                        .sourceCode("https://github.com/WiIIiam278/HuskTowns")
                        .website("https://william278.net/project/husktowns")
                        .support("https://discord.gg/tVYhJfyDWG")
                        .build())
                .fileInclusionRules(List.of(
                        DumpOptions.FileInclusionRule.configFile("config.yml", "Config File"),
                        DumpOptions.FileInclusionRule.configFile("flags.yml", "Flags File"),
                        DumpOptions.FileInclusionRule.configFile("levels.yml", "Town Levels File"),
                        DumpOptions.FileInclusionRule.configFile("roles.yml", "Town Roles File"),
                        DumpOptions.FileInclusionRule.configFile("rules.yml", "Rule Presets File"),
                        DumpOptions.FileInclusionRule.configFile(getMessagesFile(), "Locales File")
                ))
                .compatibilityRules(List.of(
                        getCompatibilityWarning("CMI", "CMI may cause compatibility issues with " +
                                "HuskTowns. If you're using Vault, ensure the CMI-compatible version is in use.")
                ))
                .build();
    }

    @NotNull
    @Blocking
    private PluginStatus getPluginStatus() {
        return PluginStatus.builder()
                .blocks(List.of(getSystemStatus(), getTownStatus(), getHookStatus(), getOperationTypeStatus()))
                .build();
    }

    @NotNull
    @Blocking
    private PluginStatus.MapStatusBlock getSystemStatus() {
        return new PluginStatus.MapStatusBlock(
                Map.of(
                        "Language", StatusLine.LANGUAGE.getValue(getPlugin()),
                        "Database Type", StatusLine.DATABASE_TYPE.getValue(getPlugin()),
                        "Database Local", StatusLine.IS_DATABASE_LOCAL.getValue(getPlugin()),
                        "Cross Server", StatusLine.IS_CROSS_SERVER.getValue(getPlugin()),
                        "Server Name", StatusLine.SERVER_NAME.getValue(getPlugin()),
                        "Message Broker", StatusLine.MESSAGE_BROKER_TYPE.getValue(getPlugin()),
                        "Redis Sentinel", StatusLine.USING_REDIS_SENTINEL.getValue(getPlugin()),
                        "Redis Password", StatusLine.USING_REDIS_PASSWORD.getValue(getPlugin()),
                        "Redis SSL", StatusLine.REDIS_USING_SSL.getValue(getPlugin()),
                        "Redis Local", StatusLine.IS_REDIS_LOCAL.getValue(getPlugin())
                ),
                "Plugin Status", "fa6-solid:wrench"
        );
    }

    @NotNull
    @Blocking
    private PluginStatus.ChartStatusBlock getTownStatus() {
        return new PluginStatus.ChartStatusBlock(
                getPlugin().getClaimWorlds().entrySet().stream()
                        .map((e) -> Map.entry(
                                new PluginStatus.ChartKey(e.getKey()),
                                e.getValue().getClaimCount()
                        ))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                PluginStatus.ChartType.PIE, "Town Claims by Worlds", "mdi:square"
        );
    }

    @NotNull
    @Blocking
    private PluginStatus.ListStatusBlock getHookStatus() {
        return new PluginStatus.ListStatusBlock(
                getPlugin().getHookManager().getHooks().stream().map(Hook::getHookInfo).map(PluginHook::id).toList(),
                "Loaded Hooks", "fa6-solid:plug"
        );
    }

    @NotNull
    @Blocking
    private PluginStatus.ListStatusBlock getOperationTypeStatus() {
        return new PluginStatus.ListStatusBlock(
                getPlugin().getOperationListener().getRegisteredOperationTypes().stream()
                        .map(OperationType::asMinimalString).toList(),
                "Operation Types", "ci:flag"
        );
    }

    @NotNull
    @SuppressWarnings("SameParameterValue")
    private DumpOptions.CompatibilityRule getCompatibilityWarning(@NotNull String plugin, @NotNull String description) {
        return DumpOptions.CompatibilityRule.builder()
                .labelToApply(new PluginInfo.Label("Warning", "#fcba03", description))
                .resourceName(plugin).build();
    }

    @NotNull
    private String getMessagesFile() {
        return "messages-%s.yml".formatted(getPlugin().getSettings().getLanguage());
    }

    private int getColorFor(@NotNull String seed) {
        int hash = seed.hashCode();
        return new Color((hash >> 16) & 0xFF, (hash >> 8) & 0xFF, hash & 0xFF).getRGB();
    }

    @NotNull
    HuskTowns getPlugin();

}
