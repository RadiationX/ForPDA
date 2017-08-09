package forpdateam.ru.forpda.api.ndevdb.models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 06.08.17.
 */

public class Brands {

    public final static Pattern LETTERS_PATTERN = Pattern.compile("<div class=\"letter\">([^<]*?)<\\/div>[^<]*?<div class=\"frame\">([\\s\\S]*?)<\\/div>");
    public final static Pattern ITEMS_IN_LETTER_PATTERN = Pattern.compile("<a href=\"[^\"]*?\\/([^\\/\"]*?)(?:\\/all)?\">([\\s\\S]*?) ?\\((\\d+)\\)<\\/a>");

    private LinkedHashMap<String, ArrayList<Item>> letterMap = new LinkedHashMap<>();
    private String catId;
    private String catTitle;
    private int actual = 0;
    private int all = 0;

    public String getCatId() {
        return catId;
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    public String getCatTitle() {
        return catTitle;
    }

    public void setCatTitle(String catTitle) {
        this.catTitle = catTitle;
    }

    public int getActual() {
        return actual;
    }

    public void setActual(int actual) {
        this.actual = actual;
    }

    public int getAll() {
        return all;
    }

    public void setAll(int all) {
        this.all = all;
    }

    public void putItems(String letter, ArrayList<Item> items) {
        letterMap.put(letter, items);
    }

    public LinkedHashMap<String, ArrayList<Item>> getLetterMap() {
        return letterMap;
    }

    public static class Item {
        private String title;
        private String id;
        private int count = 0;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
