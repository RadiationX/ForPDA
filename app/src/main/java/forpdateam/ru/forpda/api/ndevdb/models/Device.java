package forpdateam.ru.forpda.api.ndevdb.models;

import android.util.Pair;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 06.08.17.
 */

public class Device {
    public final static Pattern PATTERN_1 = Pattern.compile("h1 class=\"product-name\">(?:<a[^>]*?>[^<]*?<\\/a>)? ?([^<]*?)<\\/h1>[\\s\\S]*?div class=\"item-visual\">([\\s\\S]*?)<\\/div>[^<]*?<div class=\"item-info\">[\\s\\S]*?div class=\"item-content[^>]*?>[^<]*?<div class=\"content\">([\\s\\S]*?)<\\/div>[^<]*?<div class=\"aside\">");
    public final static Pattern IMAGES_PATTERN = Pattern.compile("<a[^>]*?href=\"([^\"]*?)\"[^>]*?><img src=\"([^\"]*?)\"");
    public final static Pattern SPECS_TITLED_PATTERN = Pattern.compile("<div class=\"specifications-list\"><h3[^>]*?>([^>]*?)<\\/h3>([\\s\\S]*?)<\\/div>");

    private ArrayList<Pair<String, ArrayList<Pair<String, String>>>> specs = new ArrayList<>();
    private ArrayList<Pair<String, String>> images = new ArrayList<>();
    private Pair<Integer, String> rating;
    private String id;
    private String title;
    private String manId;
    private String manTitle;
    private String catId;
    private String catTitle;

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

    public String getManId() {
        return manId;
    }

    public void setManId(String manId) {
        this.manId = manId;
    }

    public String getManTitle() {
        return manTitle;
    }

    public void setManTitle(String manTitle) {
        this.manTitle = manTitle;
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

    public void addImage(String imageSrc, String fullImageSrc) {
        images.add(new Pair<>(imageSrc, fullImageSrc));
    }

    public ArrayList<Pair<String, String>> getImages() {
        return images;
    }

    public void setRating(int num, String text) {
        this.rating = new Pair<>(num, text);
    }

    public Pair<Integer, String> getRating() {
        return rating;
    }

    public void addSpecs(String title, ArrayList<Pair<String, String>> specs) {
        this.specs.add(new Pair<>(title, specs));
    }

    public ArrayList<Pair<String, ArrayList<Pair<String, String>>>> getSpecs() {
        return specs;
    }
}
