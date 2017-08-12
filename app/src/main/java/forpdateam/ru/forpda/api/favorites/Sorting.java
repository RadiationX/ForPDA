package forpdateam.ru.forpda.api.favorites;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 12.08.17.
 */

public class Sorting {
    public final static String HEADER_REMEMBER = "remember";
    public final static String REMEMBER = "1";
    private final static Pattern pattern = Pattern.compile("<div class=\"forum_sort\"[^>]*?>[\\s\\S]*?<select name=\"sort_key\">[\\s\\S]*?<option value=\"([^\"]*?)\" selected(?:=\"selected\")?[^>]*?>[\\s\\S]*?<select name=\"sort_by\">[\\s\\S]*?<option value=\"([^\"]*?)\" selected(?:=\"selected\")?[^>]*?>");

    public final static class Key {
        public final static String HEADER = "sort_key";
        public final static String LAST_POST = "last_post";
        public final static String TITLE = "title";
    }

    public final static class Order {
        public final static String HEADER = "sort_by";
        public final static String DESC = "A-Z";
        public final static String ASC = "Z-A";
    }

    public Sorting() {
    }

    public Sorting(String key, String order) {
        this.key = key;
        this.order = order;
    }

    private String key = "";
    private String order = "";
    boolean remember = false;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public boolean isRemember() {
        return remember;
    }

    public void setRemember(boolean remember) {
        this.remember = remember;
    }

    public static Sorting parse(String body) {
        Sorting sorting = new Sorting();
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            switch (matcher.group(1)) {
                case Key.LAST_POST:
                    sorting.setKey(Key.LAST_POST);
                    break;
                case Key.TITLE:
                    sorting.setKey(Key.TITLE);
                    break;
            }
            switch (matcher.group(2)) {
                case Order.DESC:
                    sorting.setOrder(Order.DESC);
                    break;
                case Order.ASC:
                    sorting.setOrder(Order.ASC);
                    break;
            }
        }
        return sorting;
    }
}
