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

    public final static Pattern SPECS_TITLED_PATTERN = Pattern.compile("<div class=\"specifications-list\"><h3[^>]*?>([^>]*?)<\\/h3>([\\s\\S]*?)<\\/div>(?=<div class=\"specifications-list\">)");

    public final static Pattern REVIEWS_PATTERN = Pattern.compile("<a href=\"[^\"]*?4pda\\.ru\\/\\d+\\/\\d+\\/\\d+\\/(\\d+)\\/\" class=\"article-img\">[^<]*?<img src=\"([^\"]*?)\"[^>]*?>[^<]*?<\\/a>[^<]*?<a[^>]*?>([^<]*?)<\\/a>[\\s\\S]*?<div class=\"upd\">([^<]*?)<\\/div>[^<]*?<div class=\"description\">([^<]+?)?<\\/div>");

    public final static Pattern DISCUSS_AND_FIRM_PATTERN = Pattern.compile("<a href=\"[^\"]*?showtopic=(\\d+)\"[^>]*?>([^<]*?)<\\/a>[\\s\\S]*?<div class=\"upd\">([^<]*?)<\\/div>[^<]*?<div class=\"description\">([^<]+?)?<\\/div>");

    public final static Pattern DISCUSSIONS_PATTERN = Pattern.compile("<div class=\"tab(?: active)?\" id=\"discussions\">[\\s\\S]*?<ul class=\"article-list\">([\\s\\S]*?)<\\/ul>[^<]*?<\\/div>[^<]*?<\\/div>[^<]*?<\\/div>[^<]*?(?=<div class=\"tab(?: active)?\" id=\"reviews\">)");

    public final static Pattern FIRMwARES_PATTERN = Pattern.compile("<div class=\"tab(?: active)?\" id=\"firmware\">[\\s\\S]*?<ul class=\"article-list\">([\\s\\S]*?)<\\/ul>[^<]*?<\\/div>[^<]*?<\\/div>[^<]*?<\\/div>[^<]*?(?=<div class=\"tab(?: active)?\" id=\"prices\">)");

    public final static Pattern COMMENTS_PATTERN = Pattern.compile("<li><a name=\"comment-(\\d+)\"[^>]*?><\\/a>[^<]*?<div class=\"rating r(\\d+)\"><span[^>]*?>(\\d+)<\\/span>[\\s\\S]*?<a href=\"[^\"]*?showuser=(\\d+)\"[^>]*?>([\\s\\S]*?)<\\/a><\\/div>[^<]*?<div class=\"date\">([^<]*?)<\\/div>[^<]*?<\\/div>[^<]*?<div class=\"text-box\">((?:[^<]*?<span class=\"wo-toggle\">([\\s\\S]*?)<\\/span>)?(?:[^<]*?<span class=\"w-toggle\">([\\s\\S]*?)<\\/span>)?[\\s\\S]*?)<\\/div>[\\s\\S]*?<div class=\"profit\"[^>]*?>[^<]*?<span><a href=\"[^\"]*?\\/like\\/[^\"]*?\"[^>]*?>[^<]*?(\\d+)[^<]*?<\\/a>[\\s\\S]*?<a href=\"[^\"]*?\\/dislike\\/[^\"]*?\"[^>]*?>[^<]*?(\\d+)[^<]*?<\\/a>");

    private ArrayList<Pair<String, ArrayList<Pair<String, String>>>> specs = new ArrayList<>();
    private ArrayList<Pair<String, String>> images = new ArrayList<>();
    private Pair<Integer, String> rating;
    private ArrayList<Comment> comments = new ArrayList<>();
    private ArrayList<PostItem> discussions = new ArrayList<>();
    private ArrayList<PostItem> firmwares = new ArrayList<>();
    private ArrayList<PostItem> news = new ArrayList<>();
    private String id;
    private String title;
    private String brandId;
    private String brandTitle;
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

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getBrandTitle() {
        return brandTitle;
    }

    public void setBrandTitle(String brandTitle) {
        this.brandTitle = brandTitle;
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

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public ArrayList<PostItem> getDiscussions() {
        return discussions;
    }

    public void addDiscussion(PostItem postItem) {
        this.discussions.add(postItem);
    }

    public ArrayList<PostItem> getFirmwares() {
        return firmwares;
    }

    public void addFirmware(PostItem postItem) {
        this.firmwares.add(postItem);
    }

    public ArrayList<PostItem> getNews() {
        return news;
    }

    public void addNews(PostItem postItem) {
        this.news.add(postItem);
    }

    public static class Comment {
        private int id = 0;
        private int ratingColorCode = 0;
        private int rating = 0;
        private int userId = 0;
        private int likes = 0;
        private int dislikes = 0;
        private String nick;
        private String date;
        private String text;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getRatingColorCode() {
            return ratingColorCode;
        }

        public void setRatingColorCode(int ratingColorCode) {
            this.ratingColorCode = ratingColorCode;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getNick() {
            return nick;
        }

        public void setNick(String nick) {
            this.nick = nick;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getLikes() {
            return likes;
        }

        public void setLikes(int likes) {
            this.likes = likes;
        }

        public int getDislikes() {
            return dislikes;
        }

        public void setDislikes(int dislikes) {
            this.dislikes = dislikes;
        }
    }

    public static class PostItem{
        private int id = 0;
        private String image;
        private String title;
        private String date;
        private String desc;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
}
