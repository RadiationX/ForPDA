package forpdateam.ru.forpda.fragments.news.details;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
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
import forpdateam.ru.forpda.utils.SimpleTextWatcher;
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
    private EditText messageField;
    private FrameLayout sendContainer;
    private AppCompatImageButton buttonSend;
    private ProgressBar progressBarSend;
    private DetailsPage article;
    private ArticleCommentsAdapter adapter;
    private Comment currentReplyComment;

    public ArticleCommentsFragment setArticle(DetailsPage article) {
        this.article = article;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.article_comments, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.base_list);
        messageField = (EditText) view.findViewById(R.id.message_field);
        sendContainer = (FrameLayout) view.findViewById(R.id.send_container);
        buttonSend = (AppCompatImageButton) view.findViewById(R.id.button_send);
        progressBarSend = (ProgressBar) view.findViewById(R.id.send_progress);

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
                .subscribe(comments -> {
                    adapter.addAll(comments);
                    if (article.getCommentId() > 0) {
                        for (int i = 0; i < comments.size(); i++) {
                            if (comments.get(i).getId() == article.getCommentId()) {
                                recyclerView.scrollToPosition(i);
                                break;
                            }
                        }
                    }
                });
        recyclerView.setAdapter(adapter);

        messageField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    currentReplyComment = null;
                }
                buttonSend.setClickable(s.length() > 0);
            }
        });

        buttonSend.setOnClickListener(v -> sendComment());

        return view;
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
        if (messageField.getText().length() == 0) {
            currentReplyComment = comment;
            messageField.setText(currentReplyComment.getUserNick() + ",\n");
        } else {
            new AlertDialog.Builder(getContext())
                    .setMessage("Уже имеется введёный текст, очистить?")
                    .setPositiveButton("Да", (dialog, which) -> {
                        currentReplyComment = comment;
                        messageField.setText(currentReplyComment.getUserNick() + ",\n");
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        }

    }

    private void sendComment() {
        progressBarSend.setVisibility(View.VISIBLE);
        buttonSend.setVisibility(View.GONE);
        int commentId = currentReplyComment == null ? 0 : currentReplyComment.getId();
        RxApi.NewsList().replyComment(article, commentId, messageField.getText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(newComments -> {
                    progressBarSend.setVisibility(View.GONE);
                    buttonSend.setVisibility(View.VISIBLE);
                    messageField.setText(null);
                    currentReplyComment = null;
                    ArrayList<Comment> comments = Api.NewsApi().commentsToList(article.getCommentTree());
                    adapter.addAll(comments);
                });
    }

}
