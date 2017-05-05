package forpdateam.ru.forpda.api.topcis.models;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.api.others.pagination.Pagination;

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
    private List<TopicItem> forumItems = new ArrayList<>();
    private Pagination pagination = new Pagination();

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

    public List<TopicItem> getForumItems() {
        return forumItems;
    }

    public void addForumItem(TopicItem forumItem) {
        this.forumItems.add(forumItem);
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
