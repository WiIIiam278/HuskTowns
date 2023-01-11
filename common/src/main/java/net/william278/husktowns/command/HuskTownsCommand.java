package net.william278.husktowns.command;

import de.themoep.minedown.adventure.MineDown;
import net.william278.desertwell.AboutMenu;
import net.william278.desertwell.UpdateChecker;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.migrator.LegacyMigrator;
import net.william278.husktowns.migrator.Migrator;
import net.william278.husktowns.user.CommandUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class HuskTownsCommand extends Command {

    public HuskTownsCommand(@NotNull HuskTowns plugin) {
        super("husktowns", List.of(), plugin);
        this.setConsoleExecutable(true);
        this.setDefaultExecutor(new AboutCommand(this, plugin));
        this.setChildren(List.of(
                new ReloadCommand(this, plugin),
                new UpdateCommand(this, plugin),
                new MigrateCommand(this, plugin),
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
                    .addAttribution("Translators",
                            AboutMenu.Credit.of("Revoolt").withDescription("Spanish (es-es)"))
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

    private static class MigrateCommand extends ChildCommand implements TabProvider {
        private final List<Migrator> migrators = new ArrayList<>();

        protected MigrateCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("migrate", List.of(), parent, "<legacy (start|set <parameter> <value>)>", plugin);
            this.setConsoleExecutable(true);
            this.setOperatorCommand(true);
            this.migrators.add(new LegacyMigrator(plugin));
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final Optional<Migrator> migrator = parseStringArg(args, 0).flatMap(this::getMigrator);
            if (migrator.isEmpty()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                        .ifPresent(executor::sendMessage);
                return;
            }

            final Optional<String> subCommand = parseStringArg(args, 1);
            if (subCommand.isEmpty()) {
                executor.sendMessage(new MineDown("""
                        [[%1% Migrator] To start data migration, ensure your source database is online or the existing\s
                        SQLite database file is present in /plugins/HuskTowns/ and that the below parameters are correct.\s
                        Then, run](#00fb9a) [/husktowns migrate %2% start](#00fb9a italic run_command=/husktowns:husktowns migrate %2% start) [to start.](#00fb9a)"""
                        .replaceAll("%1%", migrator.get().getName())
                        .replaceAll("%2%", migrator.get().getName().toLowerCase())));

                migrator.get().getParameters().forEach((key, value) -> executor.sendMessage(new MineDown("""
                        [- %1%: %2%](#00fb9a run_command=/husktowns:husktowns migrate %3% set %1% )"""
                        .replaceAll("%1%", key.toLowerCase())
                        .replaceAll("%2%", value)
                        .replaceAll("%3%", migrator.get().getName().toLowerCase()))));

                executor.sendMessage(new MineDown("[[Caution]](#ffff00) [Before migration, please make sure you have " +
                                                  "configured your town Roles and Level rules to match your existing " +
                                                  migrator.get().getName().toLowerCase() + " setup!](#ffff00)"));
                if (plugin.getSettings().doCrossServer()) {
                    executor.sendMessage(new MineDown("[[Caution]](#ffff00) [Make sure all your servers are online and running " +
                                                      "HuskTowns v" + plugin.getVersion() + " to make sure that claim world data " +
                                                      "has been pre-prepared on your database for each world/server.](#ffff00)"));
                }
                executor.sendMessage(new MineDown("[[Warning]](#ff0000) [If you proceed with migration, any existing town data " +
                                                  "will be deleted](#ff0000)"));
                return;
            }

            switch (subCommand.get()) {
                case "start" -> migrator.get().start(executor);
                case "set" -> {
                    final Optional<String> parameter = parseStringArg(args, 2).map(String::toUpperCase);
                    final Optional<String> value = parseStringArg(args, 3);
                    if (parameter.isEmpty() || value.isEmpty() || migrator.get().getParameter(parameter.get()).isEmpty()) {
                        plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                                .ifPresent(executor::sendMessage);
                        return;
                    }
                    migrator.get().setParameter(parameter.get(), value.get());
                    executor.sendMessage(new MineDown("[[%1% Migrator] Set parameter %2% to %3%.](#00fb9a)"
                            .replaceAll("%1%", migrator.get().getName())
                            .replaceAll("%2%", parameter.get().toLowerCase())
                            .replaceAll("%3%", value.get())));
                }
                default -> plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                        .ifPresent(executor::sendMessage);
            }
        }

        private Optional<Migrator> getMigrator(@NotNull String name) {
            return migrators.stream().filter(migrator -> migrator.getName().equalsIgnoreCase(name)).findFirst();
        }

        @Override
        @Nullable
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return switch (args.length) {
                case 0, 1 -> filter(migrators.stream().map(Migrator::getName).map(String::toLowerCase).toList(), args);
                case 2 -> filter(List.of("start", "set"), args);
                case 3 -> filter(migrators.stream()
                        .filter(migrator -> migrator.getName().equalsIgnoreCase(args[0]))
                        .flatMap(migrator -> migrator.getParameters().keySet()
                                .stream().map(String::toLowerCase)).toList(), args);
                default -> List.of();
            };
        }
    }

}
