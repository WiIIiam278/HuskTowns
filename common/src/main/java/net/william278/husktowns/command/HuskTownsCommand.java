package net.william278.husktowns.command;

import net.william278.desertwell.AboutMenu;
import net.william278.desertwell.UpdateChecker;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class HuskTownsCommand extends Command {

    public HuskTownsCommand(@NotNull HuskTowns plugin) {
        super("husktowns", List.of(), plugin);
        this.setConsoleExecutable(true);
        this.setDefaultExecutor(new AboutCommand(this, plugin));
        this.setChildren(List.of(
                new ReloadCommand(this, plugin),
                new UpdateCommand(this, plugin),
                getHelpCommand(),
                (ChildCommand) getDefaultExecutor()
        ));
    }

    private static class AboutCommand extends ChildCommand {
        private final AboutMenu aboutMenu;

        protected AboutCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("about", List.of("info"), parent, "", plugin);
            this.setConsoleExecutable(true);
            this.aboutMenu = AboutMenu.create("HuskTowns")
                    .withDescription("Simple and elegant proxy-compatible Towny-style protection")
                    .withVersion(plugin.getVersion())
                    .addAttribution("Author",
                            AboutMenu.Credit.of("William278").withDescription("Click to visit website").withUrl("https://william278.net"))
                    .addAttribution("Contributors",
                            AboutMenu.Credit.of("Pacific").withDescription("Original design"))
                    .addButtons(
                            AboutMenu.Link.of("https://william278.net/docs/husktowns").withText("Documentation").withIcon("⛏"),
                            AboutMenu.Link.of("https://github.com/WiIIiam278/HuskTowns/issues").withText("Issues").withIcon("❌").withColor("#ff9f0f"),
                            AboutMenu.Link.of("https://discord.gg/tVYhJfyDWG").withText("Discord").withIcon("⭐").withColor("#6773f5"));

        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            executor.sendMessage(aboutMenu.toMineDown());
        }

    }

    private static class ReloadCommand extends ChildCommand {
        protected ReloadCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("reload", List.of(), parent, "", plugin);
            this.setConsoleExecutable(true);
            this.setOperatorCommand(true);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            plugin.reload();
            plugin.getLocales().getLocale("reloading_system")
                    .ifPresent(executor::sendMessage);
        }
    }

    private static class UpdateCommand extends ChildCommand {
        private final UpdateChecker checker;

        protected UpdateCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("update", List.of("version"), parent, "", plugin);
            this.setConsoleExecutable(true);
            this.setOperatorCommand(true);
            this.checker = plugin.getUpdateChecker();
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            checker.isUpToDate().thenAccept(upToDate -> {
                if (upToDate) {
                    plugin.getLocales().getLocale("up_to_date", plugin.getVersion().toString())
                            .ifPresent(executor::sendMessage);
                    return;
                }
                checker.getLatestVersion().thenAccept(latest -> plugin.getLocales()
                        .getLocale("update_available", latest.toString(), plugin.getVersion().toString())
                        .ifPresent(executor::sendMessage));
            });
        }
    }

}
