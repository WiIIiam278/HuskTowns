package net.william278.husktowns.gui;

import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;

public interface GUIManager {
    void openTownGUI(OnlineUser executor, Town town);

    void openTownListGUI(OnlineUser executor, Town town);

    void openDeedsGUI(OnlineUser executor, Town town);

    void openCensusGUI(OnlineUser executor, Town town);
}
