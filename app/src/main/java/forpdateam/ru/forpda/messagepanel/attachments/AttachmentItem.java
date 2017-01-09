package forpdateam.ru.forpda.messagepanel.attachments;

/**
 * Created by radiationx on 09.01.17.
 */

public class AttachmentItem {
    private String title;
    private boolean selected = false;
    private int count = 0;

    public AttachmentItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public boolean getSelected() {
        return selected;
    }

    public void toggle() {
        selected = !selected;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
