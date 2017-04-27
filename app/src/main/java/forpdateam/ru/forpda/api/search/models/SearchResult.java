package forpdateam.ru.forpda.api.search.models;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.api.others.pagination.Pagination;

/**
 * Created by radiationx on 01.02.17.
 */

public class SearchResult {
    private List<SearchItem> items = new ArrayList<>();
    private SearchSettings settings;
    private Pagination pagination = new Pagination();
    private String html;

    public List<SearchItem> getItems() {
        return items;
    }

    public void addItem(SearchItem item) {
        items.add(item);
    }

    public SearchSettings getSettings() {
        return settings;
    }

    public void setSettings(SearchSettings settings) {
        this.settings = settings;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
