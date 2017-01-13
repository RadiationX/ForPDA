package forpdateam.ru.forpda.api.theme.editpost;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.RequestFile;
import io.reactivex.Observable;

/**
 * Created by radiationx on 10.01.17.
 */

public class EditPost {
    private final static Pattern loadedAttachments = Pattern.compile("add_current_item\\([^'\"]*?['\"](\\d+)['\"],[^'\"]*?['\"]([^'\"]*\\.(\\w*))['\"],[^'\"]*?['\"]([^'\"]*?)['\"],[^'\"]*?['\"][^'\"]*\\/(\\w*)\\.[^\"']*?['\"]");
    private final static Pattern statusInfo = Pattern.compile("can_upload = parseInt\\([\"'](\\d+)'\\)[\\s\\S]*?status_msg_files = .([\\s\\S]*?).;[\\s\\S]*?status_msg = .([\\s\\S]*?).;[\\s\\S]*?status_is_error = ([\\s\\S]*?);");

    public Observable<EditPostForm> loadForm(int id) {
        return Observable.fromCallable(() -> _loadForm(id));
    }

    public Observable<List<AttachmentItem>> uploadFiles(List<RequestFile> files) {
        return uploadFiles(0, files);
    }

    public Observable<List<AttachmentItem>> uploadFiles(final int id, List<RequestFile> files) {
        return Observable.fromCallable(() -> _uploadFiles(id, files));
    }

    public Observable<List<AttachmentItem>> deleteFiles(List<AttachmentItem> items) {
        return deleteFiles(0, items);
    }

    public Observable<List<AttachmentItem>> deleteFiles(final int id, List<AttachmentItem> items) {
        return Observable.fromCallable(() -> _deleteFiles(id, items));
    }

    private EditPostForm _loadForm(int postId) throws Exception {
        String response = Client.getInstance().get("http://4pda.ru/forum/index.php?&act=attach&code=attach_upload_show&attach_rel_id=".concat(Integer.toString(postId)));
        return parseForm(response);
    }

    private List<AttachmentItem> _uploadFiles(int postId, List<RequestFile> files) throws Exception {
        String url = "http://4pda.ru/forum/index.php?&act=attach&code=attach_upload_process&attach_rel_id=".concat(Integer.toString(postId));
        List<AttachmentItem> items = new ArrayList<>();
        AttachmentItem item;
        String response;
        Matcher matcher;
        for (RequestFile file : files) {
            file.setRequestName("FILE_UPLOAD[]");
            item = new AttachmentItem();

            response = Client.getInstance().post(url, null, file);
            matcher = loadedAttachments.matcher(response);
            if (matcher.find())
                item = fillAttachment(item, matcher);

            matcher = statusInfo.matcher(response);
            if (matcher.find())
                fillAttachmentStatus(item, matcher);
            items.add(item);
        }
        return items;
    }

    private EditPostForm parseForm(String response) {
        EditPostForm form = new EditPostForm();
        Matcher matcher = statusInfo.matcher(response);
        if (matcher.find()) {
            form.setCanUpload(matcher.group(1).equals("1"));
            form.setStatusFile(matcher.group(2));
            form.setStatus(matcher.group(3));
            form.setError(!matcher.group(4).equals("0"));
        }

        matcher = loadedAttachments.matcher(response);
        while (matcher.find())
            form.addAttachment(fillAttachment(new AttachmentItem(), matcher));

        return form;
    }

    private List<AttachmentItem> _deleteFiles(int postId, List<AttachmentItem> items) throws Exception {
        String response;
        Matcher matcher;
        for (AttachmentItem item : items) {
            response = Client.getInstance().get("http://4pda.ru/forum/index.php?&act=attach&code=attach_upload_remove&attach_rel_id=".concat(Integer.toString(postId)).concat("&attach_id=").concat(Integer.toString(item.getId())));
            matcher = statusInfo.matcher(response);
            if (matcher.find())
                fillAttachmentStatus(item, matcher);
        }
        return items;
    }

    private AttachmentItem fillAttachment(AttachmentItem item, Matcher matcher) {
        item.setId(Integer.parseInt(matcher.group(1)));
        item.setName(matcher.group(2));
        item.setFormat(matcher.group(3));
        item.setWeight(matcher.group(4));
        if (item.getTypeFile() == AttachmentItem.TYPE_IMAGE) {
            item.setImageUrl("http://cs5-2.4pda.to/".concat(Integer.toString(item.getId())).concat(".").concat(item.getFormat()));
        }
        item.setLoadState(AttachmentItem.STATE_LOADED);
        return item;
    }

    private AttachmentItem fillAttachmentStatus(AttachmentItem item, Matcher matcher) {
        item.setStatus(getStatus(matcher.group(3)));
        item.setError(!matcher.group(4).equals("0"));
        return item;
    }

    private int getStatus(String status) {
        switch (status) {
            case "attach_removed":
                return AttachmentItem.STATUS_REMOVED;
            case "upload_no_file":
                return AttachmentItem.STATUS_NO_FILE;
            case "upload_ok":
                return AttachmentItem.STATUS_UPLOADED;
            case "ready":
                return AttachmentItem.STATUS_READY;
        }
        return AttachmentItem.STATUS_UNKNOWN;
    }
}
