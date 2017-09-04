package forpdateam.ru.forpda.fragments.news.details;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.news.models.Comment;
import forpdateam.ru.forpda.api.news.models.DetailsPage;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.fragments.devdb.BrandFragment;
import forpdateam.ru.forpda.fragments.theme.ThemeHelper;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.IntentHandler;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 03.09.17.
 */

public class ArticleCommentsFragment extends Fragment implements ArticleCommentsAdapter.ClickListener {
    private RecyclerView recyclerView;
    private DetailsPage article;
    ArticleCommentsAdapter adapter;

    public ArticleCommentsFragment setArticle(DetailsPage article) {
        this.article = article;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = new RecyclerView(getContext());
        recyclerView.setBackgroundColor(App.getColorFromAttr(getContext(), R.attr.background_for_lists));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new BrandFragment.SpacingItemDecoration(App.px12, false));
        adapter = new ArticleCommentsAdapter();
        adapter.setClickListener(this);

        Observable.fromCallable(() -> {
            if (article.getCommentTree() == null) {
                Comment commentTree = Api.NewsApi().parseComments(article.getKarmaMap(), article.getCommentsSource());
                article.setCommentTree(commentTree);
            }
            return Api.NewsApi().commentsToList(article.getCommentTree());
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter::addAll);


        recyclerView.setAdapter(adapter);

        return recyclerView;
    }

    @Override
    public void onNickClick(Comment comment, int position) {
        IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=" + comment.getUserId());
    }

    @Override
    public void onLikeClick(Comment comment, int position) {
        RxApi.NewsList().likeComment(article.getId(), comment.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    Comment.Karma karma = comment.getKarma();
                    karma.setStatus(Comment.Karma.LIKED);
                    karma.setCount(karma.getCount() + 1);
                    adapter.notifyItemChanged(position);
                });
    }

    @Override
    public void onReplyClick(Comment comment, int position) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.report_layout, null);

        assert layout != null;
        final EditText messageField = (EditText) layout.findViewById(R.id.report_text_field);

        new AlertDialog.Builder(getContext())
                .setTitle("Ответ на коммент ".concat(comment.getUserNick()))
                .setView(layout)
                .setPositiveButton("Отправить", (dialogInterface, i) -> {
                    RxApi.NewsList().replyComment(article, comment.getId(), messageField.getText().toString())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(newComments -> {
                                ArrayList<Comment> comments = Api.NewsApi().commentsToList(article.getCommentTree());
                                adapter.addAll(comments);
                            });
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}
