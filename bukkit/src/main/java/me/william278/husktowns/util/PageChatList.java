package me.william278.husktowns.util;

import de.themoep.minedown.MineDown;
import me.william278.husktowns.MessageManager;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.ArrayList;

public class PageChatList {

    private final ArrayList<String> items;
    private final String pageChangeCommand;
    private final int itemsPerPage;
    private final int maxPage;

    public PageChatList(ArrayList<String> items, int itemsPerPage, String pageChangeCommand) {
        this.pageChangeCommand = pageChangeCommand;
        this.items = new ArrayList<>();
        this.itemsPerPage = itemsPerPage;
        this.items.addAll(items);
        this.maxPage = (int) Math.ceil(((double) items.size()) / itemsPerPage);
    }

    public boolean doesNotContainPage(int pageNo) {
        return pageNo > items.size();
    }

    public BaseComponent[] getPage(int pageNo) {
        StringBuilder builder = new StringBuilder();
        int indexStart = ((pageNo - 1) * itemsPerPage) + 1;
        int indexEnd = (indexStart - 1) + itemsPerPage;

        for (int i = indexStart - 1; (i <= (indexEnd - 1)) && (i <= (items.size() - 1)); i++) {
            String item = items.get(i);
            builder.append(item).append("\n");
        }

        // Send a message if there's no items on the page
        if (builder.toString().equals("")) {
            builder.append(MessageManager.getRawMessage("page_no_items")).append("\n");
        }

        if (pageNo == 1) {
            if (pageNo == maxPage) {
                builder.append(MessageManager.getRawMessage("page_options_min_max",
                        Integer.toString(pageNo), Integer.toString(maxPage)));
            } else {
                builder.append(MessageManager.getRawMessage("page_options_min",
                        Integer.toString(pageNo), Integer.toString(maxPage), pageChangeCommand + " " + (pageNo + 1)));
            }
        } else if (pageNo == maxPage) {
            builder.append(MessageManager.getRawMessage("page_options_max",
                    pageChangeCommand + " " + (pageNo - 1), Integer.toString(pageNo), Integer.toString(maxPage)));
        } else {
            builder.append(MessageManager.getRawMessage("page_options",
                    pageChangeCommand + " " + (pageNo - 1), Integer.toString(pageNo),
                    Integer.toString(maxPage), pageChangeCommand + " " + (pageNo + 1)));
        }
        return new MineDown(builder.toString()).toComponent();
    }
}