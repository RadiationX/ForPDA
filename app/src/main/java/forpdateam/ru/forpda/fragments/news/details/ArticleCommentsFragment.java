package forpdateam.ru.forpda.fragments.news.details;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.news.models.Comment;
import forpdateam.ru.forpda.api.news.models.DetailsPage;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.fragments.devdb.BrandFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.SimpleTextWatcher;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 03.09.17.
 */

public class ArticleCommentsFragment extends Fragment implements ArticleCommentsAdapter.ClickListener {
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private EditText messageField;
    private FrameLayout sendContainer;
    private AppCompatImageButton buttonSend;
    private ProgressBar progressBarSend;
    private RelativeLayout writePanel;
    private DetailsPage article;
    private ArticleCommentsAdapter adapter;
    private Comment currentReplyComment;

    private Observer loginObserver = (observable, o) -> {
        if (o == null) o = false;
        if (ClientHelper.getAuthState()) {
            writePanel.setVisibility(View.VISIBLE);
        } else {
            writePanel.setVisibility(View.GONE);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    };

    public ArticleCommentsFragment setArticle(DetailsPage article) {
        this.article = article;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.article_comments, container, false);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_list);
        recyclerView = (RecyclerView) view.findViewById(R.id.base_list);
        writePanel = (RelativeLayout) view.findViewById(R.id.comment_write_panel);
        messageField = (EditText) view.findViewById(R.id.message_field);
        sendContainer = (FrameLayout) view.findViewById(R.id.send_container);
        buttonSend = (AppCompatImageButton) view.findViewById(R.id.button_send);
        progressBarSend = (ProgressBar) view.findViewById(R.id.send_progress);

        refreshLayout.setProgressBackgroundColorSchemeColor(App.getColorFromAttr(getContext(), R.attr.colorPrimary));
        refreshLayout.setColorSchemeColors(App.getColorFromAttr(getContext(), R.attr.colorAccent));
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);
            RxApi.NewsList().getDetails(article.getId())
                    .map(page -> {
                        Comment commentTree = Api.NewsApi().updateComments(article, page);
                        article.setCommentTree(commentTree);
                        return Api.NewsApi().commentsToList(article.getCommentTree());
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(comments -> {
                        refreshLayout.setRefreshing(false);
                        adapter.addAll(comments);
                    });
        });

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

        if (ClientHelper.getAuthState()) {
            writePanel.setVisibility(View.VISIBLE);
        } else {
            writePanel.setVisibility(View.GONE);
        }

        ClientHelper.getInstance().addLoginObserver(loginObserver);
        return view;
    }

    @Override
    public void onNickClick(Comment comment, int position) {
        IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=" + comment.getUserId());
    }

    @Override
    public void onLikeClick(Comment comment, int position) {
        Comment.Karma karma = comment.getKarma();
        karma.setStatus(Comment.Karma.LIKED);
        karma.setCount(karma.getCount() + 1);
        adapter.notifyItemChanged(position);
        RxApi.NewsList().likeComment(article.getId(), comment.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void onReplyClick(Comment comment, int position) {
        if (messageField.getText().length() == 0) {
            fillMessageField(comment);
        } else {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.comment_reply_warning)
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> fillMessageField(comment))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }

    }

    private void fillMessageField(Comment comment) {
        currentReplyComment = comment;
        messageField.setText(currentReplyComment.getUserNick() + ",\n");
        messageField.setSelection(messageField.getText().length());
        messageField.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(messageField, InputMethodManager.SHOW_IMPLICIT);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        ClientHelper.getInstance().removeLoginObserver(loginObserver);
    }
}
