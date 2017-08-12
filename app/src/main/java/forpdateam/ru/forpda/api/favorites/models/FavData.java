package forpdateam.ru.forpda.api.favorites.models;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.api.favorites.Sorting;
import forpdateam.ru.forpda.api.others.pagination.Pagination;

/**
 * Created by radiationx on 22.09.16.
 */

public class FavData {
    private List<FavItem> items = new ArrayList<>();
    private Pagination pagination = new Pagination();
    private Sorting sorting = new Sorting();

    public List<FavItem> getItems() {
        return items;
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

    public Sorting getSorting() {
        return sorting;
    }

    public void setSorting(Sorting sorting) {
        this.sorting = sorting;
    }
}
