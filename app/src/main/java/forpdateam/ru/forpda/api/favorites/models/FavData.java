package forpdateam.ru.forpda.api.favorites.models;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.api.others.pagination.Pagination;

/**
 * Created by radiationx on 22.09.16.
 */

public class FavData {
    private List<FavItem> allItems = new ArrayList<>();
    private List<FavItem> pinnedItems = new ArrayList<>();
    private List<FavItem> items = new ArrayList<>();

    private Pagination pagination = new Pagination();

    public void addToAllItem(FavItem item) {
        allItems.add(item);
    }

    public List<FavItem> getAllItems() {
        return allItems;
    }

    public List<FavItem> getItems() {
        return items;
    }

    public List<FavItem> getPinnedItems() {
        return pinnedItems;
    }

    public void addPinnedItem(FavItem item) {
        pinnedItems.add(item);
    }

    public void addItem(FavItem item) {
        items.add(item);
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
