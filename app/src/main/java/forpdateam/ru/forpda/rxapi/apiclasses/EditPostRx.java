package forpdateam.ru.forpda.rxapi.apiclasses;

import java.util.List;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.RequestFile;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import io.reactivex.Observable;

/**
 * Created by radiationx on 25.03.17.
 */

public class EditPostRx {

    public Observable<EditPostForm> loadForm(int postId) {
        return Observable.fromCallable(() -> Api.EditPost().loadForm(postId));
    }

    public Observable<List<AttachmentItem>> uploadFiles(final int id, List<RequestFile> files) {
        return Observable.fromCallable(() -> Api.EditPost().uploadFiles(id, files));
    }

    public Observable<List<AttachmentItem>> deleteFiles(final int id, List<AttachmentItem> items) {
        return Observable.fromCallable(() -> Api.EditPost().deleteFiles(id, items));
    }

    public Observable<ThemePage> sendPost(EditPostForm form) {
        return Observable.fromCallable(() -> ThemeRx.transform(Api.EditPost().sendPost(form), true));
    }
}
