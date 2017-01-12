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

    public EditPostForm() {}

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
