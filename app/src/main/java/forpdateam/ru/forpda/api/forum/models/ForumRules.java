package forpdateam.ru.forpda.api.forum.models;

import java.util.ArrayList;

/**
 * Created by radiationx on 16.10.17.
 */

public class ForumRules {
    private ArrayList<Item> items = new ArrayList<>();
    private String html;
    private String date;

    public ArrayList<Item> getItems() {
        return items;
    }

    public void addItem(Item item) {
        this.items.add(item);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public static class Item {
        private String number;
        private String text;
        private boolean header = false;

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public boolean isHeader() {
            return header;
        }

        public void setHeader(boolean header) {
            this.header = header;
        }
    }

}
