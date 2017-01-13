package forpdateam.ru.forpda.api.theme.editpost.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 10.01.17.
 */

public class EditPostForm {
    private List<AttachmentItem> loadedAttachments = new ArrayList<>();
    private boolean canUpload = false, error = true;
    private String statusFile;
    private String status;
    private String message;

    private int forumId = 0, topicId = 0, postId = 0, st = 0;
    private int[] attachments;

    public EditPostForm() {}

    public void setLoadedAttachments(List<AttachmentItem> loadedAttachments) {
        this.loadedAttachments = loadedAttachments;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getForumId() {
        return forumId;
    }

    public void setForumId(int forumId) {
        this.forumId = forumId;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getSt() {
        return st;
    }

    public void setSt(int st) {
        this.st = st;
    }

    public int[] getAttachments() {
        return attachments;
    }

    public void setAttachments(int[] attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(AttachmentItem item) {
        loadedAttachments.add(item);
    }

    public List<AttachmentItem> getLoadedAttachments() {
        return loadedAttachments;
    }

    public boolean isCanUpload() {
        return canUpload;
    }

    public void setCanUpload(boolean canUpload) {
        this.canUpload = canUpload;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getStatusFile() {
        return statusFile;
    }

    public void setStatusFile(String statusFile) {
        this.statusFile = statusFile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
