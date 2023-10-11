package net.william278.husktowns.gui.list;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.gui.GuiSettings;
import net.william278.husktowns.gui.IGuiSettings;
import net.william278.husktowns.gui.PagedItemsGuiAbstract;
import net.william278.husktowns.town.Spawn;
import net.william278.husktowns.town.Town;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;
import xyz.xenondevs.invui.window.Window;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

public class TownListGui extends PagedItemsGuiAbstract {
    private final BukkitHuskTowns plugin;
    private final Set<Filter> filters = EnumSet.noneOf(Filter.class);

    public TownListGui(BukkitHuskTowns plugin) {
        super(9, 4, true, 3);
        this.plugin = plugin;
        GuiSettings.SingleGuiSettings guiSettings = GuiSettings.getInstance().getTownListGuiSettings();
        applyStructure(new Structure(
                guiSettings.structure())
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('<', getPageButton(guiSettings, false))
                .addIngredient('>', getPageButton(guiSettings, true))
                .addIngredient('M', new FilterButton(Filter.MONEY, this))
                .addIngredient('m', new FilterButton(Filter.MEMBERS, this))
                .addIngredient('T', new FilterButton(Filter.TERRITORIES, this))
                .addIngredient('P', new FilterButton(Filter.PUBLIC, this))
                .addIngredient('p', new FilterButton(Filter.PRIVATE, this))
        );
        setContent(plugin.getTowns().stream()
                .map(town -> (Item) new TownItem(town))
                .toList());
    }

    public void open(Player player) {
        Window.single()
                .setTitle("Town list")
                .setGui(this)
                .open(player);
    }

    public void toggleFilter(Filter filter) {
        if (filters.contains(filter)) {
            filters.remove(filter);
        } else {
            filters.add(filter);
        }
        Stream<Town> townStream = plugin.getTowns().stream();
        for (Filter filter1 : filters) {

            switch (filter1) {
                case MONEY ->
                        townStream = townStream.sorted((town1, town2) -> town2.getMoney().intValue() - town1.getMoney().intValue());
                case MEMBERS ->
                        townStream = townStream.sorted((town1, town2) -> town2.getMembers().size() - town1.getMembers().size());
                case TERRITORIES ->
                        townStream = townStream.sorted((town1, town2) -> town2.getClaimCount() - town1.getClaimCount());
                case PUBLIC ->
                        townStream = townStream.filter(town -> town.getSpawn().map(Spawn::isPublic).orElse(false));
                case PRIVATE ->
                        townStream = townStream.filter(town -> town.getSpawn().map(spawn -> !spawn.isPublic()).orElse(true));
            }
        }

        setContent(townStream.map(town -> (Item) new TownItem(town)).toList());
    }

    public AbstractItem getPageButton(GuiSettings.SingleGuiSettings guiSettings, boolean forward) {
        return new PageItem(forward) {
            @Override
            public ItemProvider getItemProvider(PagedGui<?> gui) {
                return forward ?
                        guiSettings.getItem("forwardButton").toItemProvider() :
                        guiSettings.getItem("backButton").toItemProvider();
            }
        };
    }


    private static class FilterButton extends AbstractItem {

        private final Filter filter;
        private final TownListGui townListGui;

        private FilterButton(Filter filter, TownListGui townListGui) {
            this.filter = filter;
            this.townListGui = townListGui;
        }

        @Override
        public ItemProvider getItemProvider() {
            GuiSettings.SingleGuiSettings guiSettings = GuiSettings.getInstance().getTownListGuiSettings();
            if (townListGui.filters.contains(filter))
                return guiSettings.getItem("publicFilterItem").toItemProvider("%filter%", filter.name());
            else
                return switch (filter) {
                    case MONEY -> guiSettings.getItem("moneyFilterItem").toItemProvider();
                    case MEMBERS -> guiSettings.getItem("membersFilterItem").toItemProvider();
                    case TERRITORIES -> guiSettings.getItem("territoriesFilterItem").toItemProvider();
                    case PUBLIC -> guiSettings.getItem("publicFilterItem").toItemProvider();
                    case PRIVATE -> guiSettings.getItem("privateFilterItem").toItemProvider();
                };
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            townListGui.toggleFilter(filter);
            townListGui.open(player);
        }
    }

    public enum Filter {
        MONEY,
        MEMBERS,
        TERRITORIES,
        PUBLIC,
        PRIVATE
    }
}
