package forpdateam.ru.forpda.api.news.models;

import android.util.SparseArray;

import java.util.ArrayList;

/**
 * Created by isanechek on 7/20/17.
 */
// На время только, ибо оригинал в котлине
public class DetailsPage {
    private int id;
    private int commentId;
    private int authorId;
    private String url;
    private String title;
    private String description;
    private String author;
    private String date;
    private String imgUrl;
    private int commentsCount;
    private ArrayList<Tag> tags = new ArrayList<>();
    private SparseArray<Comment.Karma> karmaMap = new SparseArray<>();

    // for details

    private String html;
    private ArrayList<Material> materials = new ArrayList<>();
    private String navId;
    private String commentsSource;
    private Comment commentTree;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public ArrayList<Material> getMaterials() {
        return materials;
    }

    public void addMaterial(Material material) {
        this.materials.add(material);
    }

    public String getNavId() {
        return navId;
    }

    public void setNavId(String navId) {
        this.navId = navId;
    }

    public String getCommentsSource() {
        return commentsSource;
    }

    public void setCommentsSource(String commentsSource) {
        this.commentsSource = commentsSource;
    }

    public Comment getCommentTree() {
        return commentTree;
    }

    public void setCommentTree(Comment commentTree) {
        this.commentTree = commentTree;
    }

    public SparseArray<Comment.Karma> getKarmaMap() {
        return karmaMap;
    }

    public void setKarmaMap(SparseArray<Comment.Karma> karmaMap) {
        this.karmaMap = karmaMap;
    }


}
