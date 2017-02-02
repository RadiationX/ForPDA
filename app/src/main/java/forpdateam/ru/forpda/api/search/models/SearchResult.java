package forpdateam.ru.forpda.api.search.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 01.02.17.
 */

public class SearchResult {
    private List<SearchItem> items = new ArrayList<>();
    private int postsOnPageCount = 20, allPagesCount = 1, currentPage = 1;
    private SearchSettings settings;

    public List<SearchItem> getItems() {
        return items;
    }

    public void addItem(SearchItem item) {
        items.add(item);
    }

    public int getPostsOnPageCount() {
        return postsOnPageCount;
    }

    public void setPostsOnPageCount(int postsOnPageCount) {
        this.postsOnPageCount = postsOnPageCount;
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

    public SearchSettings getSettings() {
        return settings;
    }

    public void setSettings(SearchSettings settings) {
        this.settings = settings;
    }
}
