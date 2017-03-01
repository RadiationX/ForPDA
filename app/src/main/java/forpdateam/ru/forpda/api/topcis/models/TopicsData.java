package forpdateam.ru.forpda.api.topcis.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 01.03.17.
 */

public class TopicsData {
    private boolean canCreateTopic = false;
    private int id;
    private String title;
    private List<TopicItem> topicItems = new ArrayList<>();
    private List<TopicItem> pinnedItems = new ArrayList<>();
    private List<TopicItem> announceItems = new ArrayList<>();
    private int itemsPerPage = 20, allPagesCount = 1, currentPage = 1;

    public boolean canCreateTopic() {
        return canCreateTopic;
    }

    public void setCanCreateTopic(boolean canCreateTopic) {
        this.canCreateTopic = canCreateTopic;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public List<TopicItem> getTopicItems() {
        return topicItems;
    }

    public void addTopicItem(TopicItem topicItem) {
        this.topicItems.add(topicItem);
    }

    public List<TopicItem> getAnnounceItems() {
        return announceItems;
    }

    public void addAnnounceItem(TopicItem announceItem) {
        this.announceItems.add(announceItem);
    }

    public List<TopicItem> getPinnedItems() {
        return pinnedItems;
    }

    public void addPinnedItem(TopicItem pinnedItem) {
        this.pinnedItems.add(pinnedItem);
    }
}
