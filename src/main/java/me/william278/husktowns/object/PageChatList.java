package me.william278.husktowns.object;

import de.themoep.minedown.MineDown;
import me.william278.husktowns.MessageManager;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.ArrayList;
import java.util.HashMap;

public class PageChatList {

    private static final HashMap<Integer, ArrayList<String>> pages = new HashMap<>();
    private static int maxPage;
    private final String pageChangeCommand;

    public PageChatList(ArrayList<String> items, int itemsPerPage, String pageChangeCommand) {
        int index = 0;
        int page = 1;
        this.pageChangeCommand = pageChangeCommand;
        ArrayList<String> pageItems = new ArrayList<>();
        for (String item : items) {
            pageItems.add(item);
            if (index == itemsPerPage) {
                page++;
                index = 0;
                pages.put(page, pageItems);
                pageItems.clear();
                maxPage = page;
            } else {
                index++;
            }
        }
    }

    public boolean hasPage(int pageNo) {
        return pages.containsKey(pageNo);
    }

    public BaseComponent[] getPage(int pageNo) {
        StringBuilder builder = new StringBuilder();
        for (String s : pages.get(pageNo)) {
            builder.append(s).append("\n");
        }
        if (pageNo == 1) {
            builder.append(MessageManager.getRawMessage("page_options_min",
                    Integer.toString(pageNo), pageChangeCommand + " " + (pageNo+1), Integer.toString(maxPage)));
        } else if (pageNo == maxPage) {
            builder.append(MessageManager.getRawMessage("page_options_max",
                    pageChangeCommand + " " + (pageNo-1), Integer.toString(pageNo), Integer.toString(maxPage)));
        } else {
            builder.append(MessageManager.getRawMessage("page_options",
                    pageChangeCommand + " " + (pageNo-1), Integer.toString(pageNo),
                    pageChangeCommand + " " + (pageNo+1), Integer.toString(maxPage)));
        }
        return new MineDown(builder.toString()).toComponent();
    }
}
