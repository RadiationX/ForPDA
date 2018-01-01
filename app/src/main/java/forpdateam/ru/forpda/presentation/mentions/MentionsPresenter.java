package forpdateam.ru.forpda.presentation.mentions;

import com.arellomobile.mvp.InjectViewState;

import forpdateam.ru.forpda.common.mvp.BasePresenter;
import forpdateam.ru.forpda.model.repository.MentionsRepository;
import io.reactivex.disposables.Disposable;

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
public class MentionsPresenter extends BasePresenter<MentionsView> {
    private MentionsRepository mentionsRepository;

    public MentionsPresenter(MentionsRepository mentionsRepository) {
        this.mentionsRepository = mentionsRepository;
    }

    public void getMentions(int st) {
        Disposable disposable
                = mentionsRepository.getMentions(st)
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(mentionsData -> getViewState().showMentions(mentionsData), this::handleErrorRx);

        addToDisposable(disposable);
    }
}
