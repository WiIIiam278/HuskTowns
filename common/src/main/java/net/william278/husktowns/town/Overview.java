package net.william278.husktowns.town;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class Overview {

    private final HuskTowns plugin;
    private final Town town;
    private final CommandUser viewer;

    @NotNull
    public Component toComponent() {
        return getTitle()
                .append(getMeta())
                .append(getBio())
                .append(Component.newline())
                .append(getStats())
                .append(getSpawn())
                .append(Component.newline())
                .append(getButtons());
    }

    @NotNull
    private Component getTitle() {
        return plugin.getLocales().getLocale("town_overview_title",
                        town.getName(), Integer.toString(town.getId()))
                .map(mineDown -> mineDown.toComponent().append(Component.newline()))
                .orElse(Component.empty());
    }

    @NotNull
    private Component getMeta() {
        return plugin.getLocales().getLocale("town_overview_meta",
                        town.getFoundedTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        town.getFoundedTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy, HH:mm:ss")),
                        plugin.getDatabase().getUser(town.getMayor()).map(User::getUsername).orElse("???"))
                .map(mineDown -> mineDown.toComponent().append(Component.newline()))
                .orElse(Component.empty());
    }

    @NotNull
    private Component getBio() {
        return town.getBio().map(bio -> plugin.getLocales().getLocale("town_overview_bio",
                        plugin.getLocales().truncateText(bio, 45),
                        plugin.getLocales().wrapText(bio, 40))
                .map(mineDown -> mineDown.toComponent().append(Component.newline()))
                .orElse(Component.empty())).orElse(Component.empty());
    }

    @NotNull
    private Component getStats() {
        return plugin.getLocales().getLocale("town_overview_stats",
                        Long.toString(town.getLevel()),
                        "$" + new DecimalFormat("0.00").format(town.getMoney()), //todo format via Vault
                        Long.toString(town.getClaimCount()),
                        Long.toString(town.getMaxClaims()),
                        town.getColorRgb(),
                        Integer.toString(town.getMembers().size()),
                        Long.toString(town.getMaxMembers()))
                .map(mineDown -> mineDown.toComponent().append(Component.newline()))
                .orElse(Component.empty());
    }

    @NotNull
    private Component getSpawn() {
        if (!isViewerMember() && town.getSpawn().map(Spawn::isPublic).orElse(false)) {
            return Component.empty();
        }
        return town.getSpawn().map(spawn -> plugin.getLocales().getLocale("town_overview_spawn",
                        new DecimalFormat("0.0").format(spawn.getPosition().getX()),
                        new DecimalFormat("0.0").format(spawn.getPosition().getY()),
                        new DecimalFormat("0.0").format(spawn.getPosition().getZ()),
                        spawn.getPosition().getWorld().getName(),
                        new DecimalFormat("0.00").format(spawn.getPosition().getPitch()),
                        new DecimalFormat("0.00").format(spawn.getPosition().getYaw()))
                .map(mineDown -> mineDown.toComponent().append(Component.newline()))
                .orElse(Component.empty())).orElse(Component.empty());
    }

    @NotNull
    private Component getButtons() {
        return getViewButtons()
                .append(getEditButtons())
                .append(getSpawnButtons());
    }

    @NotNull
    private Component getViewButtons() {
        return plugin.getLocales().getLocale("town_button_group_view")
                .map(mineDown -> mineDown.toComponent().append(Component.space())).orElse(Component.empty())
                .append(plugin.getLocales().getLocale("town_button_members", town.getName())
                        .map(mineDown -> mineDown.toComponent().append(Component.space()))
                        .orElse(Component.empty()))
                .append(isViewerMember() ? plugin.getLocales().getLocale("town_button_claims",
                                town.getName(), town.getColorRgb())
                        .map(MineDown::toComponent)
                        .orElse(Component.empty()) : Component.empty())
                .append(Component.newline());
    }

    @NotNull
    private Component getEditButtons() {
        if (!isViewerMember()) {
            return Component.empty();
        }
        return plugin.getLocales().getLocale("town_button_group_edit")
                .map(mineDown -> mineDown.toComponent().append(Component.space())).orElse(Component.empty())
                .append(hasPrivilege(Privilege.SET_BIO) ? plugin.getLocales().getLocale("town_button_bio",
                                town.getName())
                        .map(mineDown -> mineDown.toComponent().append(Component.space()))
                        .orElse(Component.empty()) : Component.empty())
                .append(hasPrivilege(Privilege.SET_GREETING) ? plugin.getLocales().getLocale("town_button_greeting",
                                town.getName())
                        .map(mineDown -> mineDown.toComponent().append(Component.space()))
                        .orElse(Component.empty()) : Component.empty())
                .append(hasPrivilege(Privilege.SET_FAREWELL) ? plugin.getLocales().getLocale("town_button_farewell",
                                town.getName())
                        .map(mineDown -> mineDown.toComponent().append(Component.space()))
                        .orElse(Component.empty()) : Component.empty())
                .append(hasPrivilege(Privilege.SET_RULES) ? plugin.getLocales().getLocale("town_button_rules",
                                town.getName())
                        .map(MineDown::toComponent)
                        .orElse(Component.empty()) : Component.empty())
                .append(Component.newline());
    }

    @NotNull
    private Component getSpawnButtons() {
        if (town.getSpawn().isEmpty()) {
            return Component.empty();
        }
        final Spawn spawn = town.getSpawn().get();
        if (!isViewerMember() && !spawn.isPublic()) {
            return Component.empty();
        }
        return plugin.getLocales().getLocale("town_button_group_spawn")
                .map(mineDown -> mineDown.toComponent().append(Component.space()))
                .orElse(Component.empty())
                .append((isViewerMember() && hasPrivilege(Privilege.SPAWN) || spawn.isPublic())
                        ? plugin.getLocales().getLocale("town_button_spawn_teleport",
                                town.getName())
                        .map(mineDown -> mineDown.toComponent().append(Component.space()))
                        .orElse(Component.empty()) : Component.empty())
                .append((isViewerMember() && hasPrivilege(Privilege.SPAWN_PRIVACY))
                        ? plugin.getLocales().getLocale("town_button_spawn_make_" + (spawn.isPublic() ? "private" : "public"),
                        town.getName()).map(MineDown::toComponent).orElse(Component.empty()) : Component.empty());
    }


    public void show() {
        this.viewer.sendMessage(toComponent());
    }

    private Overview(@NotNull Town town, @NotNull CommandUser viewer, @NotNull HuskTowns plugin) {
        this.town = town;
        this.viewer = viewer;
        this.plugin = plugin;
    }

    @NotNull
    public static Overview of(@NotNull Town town, @NotNull CommandUser viewer, @NotNull HuskTowns plugin) {
        return new Overview(town, viewer, plugin);
    }

    private boolean isViewerMember() {
        if (viewer instanceof OnlineUser user) {
            return town.getMembers().containsKey(user.getUuid());
        }
        return false;
    }

    private boolean hasPrivilege(@NotNull Privilege privilege) {
        if (viewer instanceof OnlineUser user) {
            return plugin.getUserTown(user)
                    .map(member -> member.hasPrivilege(plugin, privilege))
                    .orElse(false);
        }
        return false;
    }

}
