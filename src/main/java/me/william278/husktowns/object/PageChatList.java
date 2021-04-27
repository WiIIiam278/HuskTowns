package me.william278.husktowns.object;

import de.themoep.minedown.MineDown;
import me.william278.husktowns.MessageManager;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.ArrayList;

public class PageChatList {

    private static final ArrayList<ArrayList<String>> pages = new ArrayList<>();
    private final String pageChangeCommand;

    public PageChatList(ArrayList<String> items, int itemsPerPage, String pageChangeCommand) {
        int index = 0;
        this.pageChangeCommand = pageChangeCommand;
        ArrayList<String> pageItems = new ArrayList<>();
        for (String item : items) {
            pageItems.add(item);
            if (index == itemsPerPage) {
                index = 0;
                pages.add(pageItems);
                pageItems.clear();
                pageItems = new ArrayList<>();
            } else {
                index++;
            }
        }
    }

    public int getMaxPage() {
        return pages.size();
    }

    public boolean hasPage(int pageNo) {
        return pageNo <= pages.size();
    }

    public BaseComponent[] getPage(int pageNo) {
        StringBuilder builder = new StringBuilder();
        for (String s : pages.get(pageNo-1)) {
            builder.append(s).append("\n");
        }
        if (pageNo == 1) {
            if (pageNo == pages.size()) {
                builder.append(MessageManager.getRawMessage("page_options_min_max",
                        Integer.toString(pageNo), Integer.toString(pages.size())));
            } else {
                builder.append(MessageManager.getRawMessage("page_options_min",
                        Integer.toString(pageNo), Integer.toString(pages.size()), pageChangeCommand + " " + (pageNo+1)));
            }
        } else if (pageNo == pages.size()) {
            builder.append(MessageManager.getRawMessage("page_options_max",
                    pageChangeCommand + " " + (pageNo-1), Integer.toString(pageNo), Integer.toString(pages.size())));
        } else {
            builder.append(MessageManager.getRawMessage("page_options",
                    pageChangeCommand + " " + (pageNo-1), Integer.toString(pageNo),
                    Integer.toString(pages.size()), pageChangeCommand + " " + (pageNo+1)));
        }
        return new MineDown(builder.toString()).toComponent();
    }
}
