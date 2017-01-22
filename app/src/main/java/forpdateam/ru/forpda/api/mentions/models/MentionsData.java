package forpdateam.ru.forpda.api.mentions.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 21.01.17.
 */

public class MentionsData {
    private List<MentionItem> items = new ArrayList<>();
    private int itemsPerPage = 20, allPagesCount = 1, currentPage = 1;

    public List<MentionItem> getItems() {
        return items;
    }

    public void addItem(MentionItem item) {
        items.add(item);
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public int getAllPagesCount() {
        return allPagesCount;
    }

    public void setAllPagesCount(int allPagesCount) {
        this.allPagesCount = allPagesCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
