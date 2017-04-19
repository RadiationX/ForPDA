package forpdateam.ru.forpda.api.theme.editpost.models;

import java.util.ArrayList;

/**
 * Created by radiationx on 10.01.17.
 */

public class EditPostForm {
    public final static String ARG_TYPE = "type";
    public final static int TYPE_NEW_POST = 0;
    public final static int TYPE_EDIT_POST = 1;
    public final static int ERROR_NONE = 0;
    public final static int ERROR_NO_PERMISSION = 1;
    private int type = TYPE_NEW_POST;
    private int errorCode = 0;
    private ArrayList<AttachmentItem> attachments = new ArrayList<>();
    private String editReason = "default_edit_reason", message = "";

    private int forumId = 0, topicId = 0, postId = 0, st = 0;

    public EditPostForm() {
    }


    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setAttachments(ArrayList<AttachmentItem> attachments) {
        this.attachments = attachments;
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

    public void addAttachment(AttachmentItem item) {
        attachments.add(item);
    }

    public ArrayList<AttachmentItem> getAttachments() {
        return attachments;
    }

    public String getEditReason() {
        return editReason;
    }

    public void setEditReason(String editReason) {
        this.editReason = editReason;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
