package forpdateam.ru.forpda.api.ndevdb.models;

import android.util.Pair;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 06.08.17.
 */

public class Brand {
    public final static Pattern DEVICES_PATTERN = Pattern.compile("<div class=\"box-holder\">[^<]*?<div class=\"visual\">[^<]*?<a[^>]*?>[^<]*?<span[^>]*?><img src=\"([^\"]*?)\"[^>]*?>[\\s\\S]*?<div class=\"name\"><a href=\"[^\"]*?devdb\\/([^\"]*?)\"[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?<div class=\"specifications-list\">([\\s\\S]*?)<\\/div>(?:<div class=\"price\">[^<]*?<strong>([^<]*?)<\\/strong>)?[\\s\\S]*?<div class=\"rating-col\">(?:[^<]*?<div class=\"rating r(\\d+)\">[^<]*?<div class=\"num\">(\\d+)<\\/div>[^<]*?<div class=\"text\">([\\s\\S]*?)<\\/div>[^<]*?<\\/div>)?");

    private ArrayList<DeviceItem> devices = new ArrayList<>();
    private String id;
    private String title;
    private String catId;
    private String catTitle;

    private int actual = 0;
    private int all = 0;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

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

    public void addDevice(DeviceItem item) {
        devices.add(item);
    }

    public ArrayList<DeviceItem> getDevices() {
        return devices;
    }

    public static class DeviceItem {
        private ArrayList<Pair<String, String>> specs = new ArrayList<>();
        private String id;
        private String title;
        private String price;
        private String imageSrc;
        private int rating = 0;

        public String getImageSrc() {
            return imageSrc;
        }

        public void setImageSrc(String imageSrc) {
            this.imageSrc = imageSrc;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        public void addSpec(String a1, String value) {
            specs.add(new Pair<>(a1, value));
        }

        public ArrayList<Pair<String, String>> getSpecs() {
            return specs;
        }
    }
}
