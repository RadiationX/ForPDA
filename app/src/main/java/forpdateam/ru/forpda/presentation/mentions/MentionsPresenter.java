package forpdateam.ru.forpda.presentation.mentions;

import android.os.Bundle;

import com.arellomobile.mvp.InjectViewState;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.mentions.models.MentionItem;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.common.mvp.BasePresenter;
import forpdateam.ru.forpda.model.repository.MentionsRepository;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
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

    public void onItemClick(MentionItem item) {
        Bundle args = new Bundle();
        args.putString(TabFragment.ARG_TITLE, item.getTitle());
        IntentHandler.handle(item.getLink(), args);
    }

    public void onItemLongClick(MentionItem item) {
        getViewState().showItemDialogMenu(item);
    }

    public void copyLink(MentionItem item) {
        Utils.copyToClipBoard(item.getLink());
    }

    public void addToFavorites(MentionItem item){
        int id = 0;
        Matcher matcher = Pattern.compile("showtopic=(\\d+)").matcher(item.getLink());
        if (matcher.find()) {
            id = Integer.parseInt(matcher.group(1));
        }
        getViewState().showAddFavoritesDialog(id);
    }
}
