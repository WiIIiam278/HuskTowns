package kalyaclaims.gui;

import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;

public interface GUIManager {
    void openTownGUI(CommandUser executor, Town town);
    void openTownListGUI(CommandUser executor, Town town);
    void openDeedsGUI(CommandUser executor, Town town);
    void openCensusGUI(CommandUser executor, Town town);


}
