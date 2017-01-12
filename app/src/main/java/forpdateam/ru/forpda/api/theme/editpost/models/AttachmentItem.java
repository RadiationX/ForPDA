package forpdateam.ru.forpda.api.theme.editpost.models;

/**
 * Created by radiationx on 09.01.17.
 */

public class AttachmentItem {
    public final static int TYPE_FILE = 0;
    public final static int TYPE_IMAGE = 1;
    public final static int STATE_NOT_LOADED = 0;
    public final static int STATE_LOADING = 1;
    public final static int STATE_LOADED = 2;
    private boolean selected = false;
    private int id = 0, type = TYPE_FILE, loadState = STATE_LOADING;
    private String name, format, weight, forumUrl, localUrl, errorMessage;

    //already added
    public AttachmentItem(int id, String name, String weight, String type) {
        this.id = id;
        this.name = name;
        this.weight = weight;
        if (type.equals("gif"))
            this.type = TYPE_IMAGE;
        this.loadState = STATE_LOADED;
    }

    //load new file
    public AttachmentItem(String name, String type) {
        this.name = name;
        if (type.contains("image"))
            this.type = TYPE_IMAGE;
        this.loadState = STATE_LOADING;
    }

    public AttachmentItem(String name) {
        this.name = name;
    }

    public AttachmentItem() {
    }

    public void createImageUrl() {
        if (type == AttachmentItem.TYPE_IMAGE)
            forumUrl = "http://cs5-2.4pda.to/".concat(Integer.toString(id)).concat(".").concat(format);
    }

    public String getForumUrl() {
        return forumUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public int getType() {
        return type;
    }

    public void setType(String type) {
        if (type.contains("gif"))
            this.type = TYPE_IMAGE;
    }

    public String getWeight() {
        return weight;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void toggle() {
        selected = !selected;
    }

    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }

    public String getLocalUrl() {
        return localUrl;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getLoadState() {
        return loadState;
    }

    public void setLoadState(int loadState) {
        this.loadState = loadState;
    }

    public String getFormat() {
        return format;
    }
}
