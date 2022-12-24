package net.william278.husktowns.command;

import net.william278.desertwell.AboutMenu;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HuskTownsCommand extends Command {

    public HuskTownsCommand(@NotNull HuskTowns plugin) {
        super("husktowns", List.of(), plugin);
        final AboutCommand ABOUT_CHILD_COMMAND = new AboutCommand(this, plugin);

        this.setConsoleExecutable(true);
        this.setDefaultExecutor(ABOUT_CHILD_COMMAND);
        this.setChildren(List.of(
                getHelpCommand(),
                ABOUT_CHILD_COMMAND
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


}
