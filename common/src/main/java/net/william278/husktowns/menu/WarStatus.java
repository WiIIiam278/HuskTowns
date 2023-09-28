package net.william278.husktowns.menu;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.User;
import net.william278.husktowns.war.War;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WarStatus {

    private static final int STATUS_BAR_BLOCK_COUNT = 10;
    private final HuskTowns plugin;
    private final War war;
    private final CommandUser viewer;

    private WarStatus(@NotNull War war, @NotNull CommandUser viewer, @NotNull HuskTowns plugin) {
        this.plugin = plugin;
        this.war = war;
        this.viewer = viewer;
    }

    @NotNull
    public static WarStatus of(@NotNull War war, @NotNull CommandUser viewer, @NotNull HuskTowns plugin) {
        return new WarStatus(war, viewer, plugin);
    }


    @NotNull
    public Component toComponent() {
        return getTitle()
                .append(getStatusBar())
                .append(Component.newline())
                .append(getWager())
                .append(getStartTime())
                .append(getLocation());
    }

    @NotNull
    private Component getTitle() {
        return plugin.getLocales().getLocale("war_status_title",
                        war.getAttacking(plugin).getName(), war.getDefending(plugin).getName())
                .map(MineDown::toComponent).orElse(Component.empty());
    }

    @NotNull
    private Component getStatusBar() {
        final float warBalance = (float) Math.max(1, war.getAliveAttackers().size())
                / (float) Math.max(1, war.getAliveDefenders().size());
        final int attackerBlocks = (int) (warBalance * STATUS_BAR_BLOCK_COUNT);

        // Build the status bar
        Component statusBar = Component.empty();
        for (int b = 0; b < STATUS_BAR_BLOCK_COUNT; b++) {
            if (b < attackerBlocks) {
                statusBar = statusBar.append(getStatusBlock(war.getAliveAttackers(), war.getAttacking(plugin)));
            } else {
                statusBar = statusBar.append(getStatusBlock(war.getAliveDefenders(), war.getDefending(plugin)));
            }
        }
        return statusBar;
    }

    @NotNull
    private Component getStatusBlock(@NotNull List<UUID> players, @NotNull Town town) {
        final List<String> usernames = new ArrayList<>(players.stream()
                .map(uuid -> plugin.getOnlineUsers().stream().filter(a -> a.getUuid().equals(uuid)).findFirst())
                .filter(Optional::isPresent).map(Optional::get)
                .map(User::getUsername).toList());
        usernames.add(String.format("+%d…", players.size() - usernames.size()));
        return plugin.getLocales().getLocale("war_status_bar_block",
                        town.getColorRgb(), town.getName(), String.join("\n", usernames))
                .map(MineDown::toComponent).orElse(Component.empty());
    }

    @NotNull
    private Component getWager() {
        return plugin.getLocales().getLocale("war_status_wager", plugin.getEconomyHook()
                .map(hook -> hook.formatMoney(war.getWager())).orElse(war.getWager().toString())
        ).map(MineDown::toComponent).orElse(Component.empty());
    }

    @NotNull
    private Component getStartTime() {
        return plugin.getLocales().getLocale("war_status_start_time",
                war.getStartTime().format(DateTimeFormatter.ofPattern("dd MMM, yyyy, HH:mm:ss"))
        ).map(MineDown::toComponent).orElse(Component.empty());
    }

    @NotNull
    private Component getLocation() {
        return plugin.getLocales().getLocale("war_status_location",
                new DecimalFormat("0.0").format(war.getDefenderSpawn().getX()),
                new DecimalFormat("0.0").format(war.getDefenderSpawn().getY()),
                new DecimalFormat("0.0").format(war.getDefenderSpawn().getZ()),
                war.getDefenderSpawn().getWorld().getName()
                        + (plugin.getSettings().doCrossServer() ? "/" + war.getHostServer() : "")
        ).map(MineDown::toComponent).orElse(Component.empty());
    }

    public void show() {
        this.viewer.sendMessage(toComponent());
    }

}
