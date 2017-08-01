package forpdateam.ru.forpda.api.reputation.models;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.api.others.pagination.Pagination;
import forpdateam.ru.forpda.api.reputation.Reputation;

/**
 * Created by radiationx on 20.03.17.
 */

public class RepData {
    private int id = 0, positive = 0, negative = 0;
    private String nick, mode = Reputation.MODE_TO, sort = Reputation.SORT_DESC;
    private Pagination pagination = new Pagination();
    private List<RepItem> items = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPositive() {
        return positive;
    }

    public void setPositive(int positive) {
        this.positive = positive;
    }

    public int getNegative() {
        return negative;
    }

    public void setNegative(int negative) {
        this.negative = negative;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public List<RepItem> getItems() {
        return items;
    }

    public void addItem(RepItem item) {
        items.add(item);
    }
}
