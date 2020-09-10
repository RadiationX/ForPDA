package forpdateam.ru.forpda.ui.fragments.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.common.AuthData;
import forpdateam.ru.forpda.entity.remote.IBaseForumPost;
import forpdateam.ru.forpda.model.AuthHolder;
import forpdateam.ru.forpda.model.preferences.OtherPreferencesHolder;
import forpdateam.ru.forpda.presentation.theme.IThemePresenter;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;

/**
 * Created by radiationx on 01.11.16.
 */

public class ThemeDialogsHelper_V2 {
    private final DynamicDialogMenu<IThemePresenter, IBaseForumPost> userMenu = new DynamicDialogMenu<>();
    private final DynamicDialogMenu<IThemePresenter, IBaseForumPost> reputationMenu = new DynamicDialogMenu<>();
    private final DynamicDialogMenu<IThemePresenter, IBaseForumPost> postMenu = new DynamicDialogMenu<>();
    private Context context;
    private AuthHolder authHolder;
    private OtherPreferencesHolder otherPreferencesHolder;

    public ThemeDialogsHelper_V2(Context context, AuthHolder authHolder, OtherPreferencesHolder otherPreferencesHolder) {
        this.context = context;
        this.authHolder = authHolder;
        this.otherPreferencesHolder = otherPreferencesHolder;

        userMenu.addItem(App.get().getString(R.string.profile), (context1, data) -> context1.openProfile(data.getId()));
        userMenu.addItem(App.get().getString(R.string.reputation), (context1, data) -> context1.onReputationMenuClick(data.getId()));
        userMenu.addItem(App.get().getString(R.string.pm_qms), (context1, data) -> context1.openQms(data.getId()));
        userMenu.addItem(App.get().getString(R.string.user_themes), (context1, data) -> context1.openSearchUserTopic(data.getId()));
        userMenu.addItem(App.get().getString(R.string.messages_in_this_theme), (context1, data) -> context1.openSearchInTopic(data.getId()));
        userMenu.addItem(App.get().getString(R.string.user_messages), (context1, data) -> context1.openSearchUserMessages(data.getId()));


        reputationMenu.addItem(App.get().getString(R.string.increase), (context1, data) -> context1.onChangeReputationClick(data.getId(), true));
        reputationMenu.addItem(App.get().getString(R.string.look), (context1, data) -> context1.openReputationHistory(data.getId()));
        reputationMenu.addItem(App.get().getString(R.string.decrease), (context1, data) -> context1.onChangeReputationClick(data.getId(), false));

        postMenu.addItem(App.get().getString(R.string.reply), (context1, data) -> context1.onReplyPostClick(data.getId()));
        postMenu.addItem(App.get().getString(R.string.quote_from_clipboard), (context1, data) -> context1.quoteFromBuffer(data.getId()));
        postMenu.addItem(App.get().getString(R.string.report), (context1, data) -> context1.onReportPostClick(data.getId()));
        postMenu.addItem(App.get().getString(R.string.edit), (context1, data) -> context1.onEditPostClick(data.getId()));
        postMenu.addItem(App.get().getString(R.string.delete), (context1, data) -> context1.onDeletePostClick(data.getId()));
        postMenu.addItem(App.get().getString(R.string.copy_link), (context1, data) -> context1.copyPostLink(data.getId()));
        postMenu.addItem(App.get().getString(R.string.create_note), (context1, data) -> context1.createNote(data.getId()));
        postMenu.addItem(App.get().getString(R.string.share), (context1, data) -> context1.sharePostLink(data.getId()));
    }

    public void showUserMenu(IThemePresenter presenter, IBaseForumPost post) {
        userMenu.disallowAll();
        userMenu.allow(0);
        userMenu.allow(1);
        AuthData authData = authHolder.get();
        if (authData.isAuth() && post.getUserId() != authData.getUserId()) {
            userMenu.allow(2);
        }
        userMenu.allow(3);
        userMenu.allow(4);
        userMenu.allow(5);
        userMenu.show(context, presenter, post);
    }

    public void showReputationMenu(IThemePresenter presenter, IBaseForumPost post) {
        reputationMenu.disallowAll();
        if (authHolder.get().isAuth()) {
            if (post.getCanPlusRep()) {
                reputationMenu.allow(0);
            }
            reputationMenu.allow(1);
            if (post.getCanMinusRep()) {
                reputationMenu.allow(2);
            }
        } else {
            reputationMenu.allow(1);
        }
        String title = App.get().getString(R.string.reputation) + " ".concat(post.getNick());
        reputationMenu.show(context, title, presenter, post);
    }

    public void showPostMenu(IThemePresenter presenter, IBaseForumPost post) {
        postMenu.disallowAll();
        if (authHolder.get().isAuth()) {
            if (post.getCanQuote()) {
                postMenu.allow(0);
                postMenu.allow(1);
            }
            if (post.getCanReport())
                postMenu.allow(2);
            if (post.getCanEdit())
                postMenu.allow(3);
            if (post.getCanDelete())
                postMenu.allow(4);
        }
        postMenu.allow(5);
        postMenu.allow(6);
        postMenu.allow(7);
        postMenu.show(context, presenter, post);
    }

    public void tryReportPost(IThemePresenter presenter, IBaseForumPost post) {
        if (otherPreferencesHolder.getShowReportWarning()) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.attention)
                    .setMessage(R.string.report_warning)
                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                        otherPreferencesHolder.setShowReportWarning(false);
                        showReportDialog(presenter, post);
                    })
                    .show();
        } else {
            showReportDialog(presenter, post);
        }
    }

    @SuppressLint("InflateParams")
    public void showReportDialog(IThemePresenter presenter, IBaseForumPost post) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.report_layout, null);

        assert layout != null;
        final EditText messageField = (EditText) layout.findViewById(R.id.report_text_field);

        new AlertDialog.Builder(context)
                .setTitle(String.format(App.get().getString(R.string.report_to_post_Nick), post.getNick()))
                .setView(layout)
                .setPositiveButton(R.string.send, (dialogInterface, i) -> presenter.reportPost(post.getId(), messageField.getText().toString()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void deletePost(IThemePresenter presenter, IBaseForumPost post) {
        new AlertDialog.Builder(context)
                .setMessage(String.format(App.get().getString(R.string.ask_delete_post_Nick), post.getNick()))
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> presenter.deletePost(post.getId()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @SuppressLint("InflateParams")
    public void changeReputation(IThemePresenter presenter, IBaseForumPost post, boolean type) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.reputation_change_layout, null);

        assert layout != null;
        final TextView text = (TextView) layout.findViewById(R.id.reputation_text);
        final EditText messageField = (EditText) layout.findViewById(R.id.reputation_text_field);
        text.setText(String.format(context.getString(R.string.change_reputation_Type_Nick), context.getString(type ? R.string.increase : R.string.decrease), post.getNick()));

        new AlertDialog.Builder(context)
                .setView(layout)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> presenter.changeReputation(post.getId(), type, messageField.getText().toString()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void votePost(IThemePresenter presenter, IBaseForumPost post, boolean type) {
        new AlertDialog.Builder(context)
                .setMessage(String.format(context.getString(R.string.change_post_reputation_Type_Nick), context.getString(type ? R.string.increase : R.string.decrease), post.getNick()))
                .setPositiveButton(R.string.ok, (dialog, which) -> presenter.votePost(post.getId(), type))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void openAnchorDialog(IThemePresenter presenter, @NotNull IBaseForumPost post, @NotNull String anchorName) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.link_to_anchor)
                .setPositiveButton(R.string.copy, (dialog, which) -> presenter.copyAnchorLink(post.getId(), anchorName))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void openSpoilerLinkDialog(IThemePresenter presenter, @NotNull IBaseForumPost post, @NotNull String spoilNumber) {
        new AlertDialog.Builder(context)
                .setMessage(R.string.spoiler_link_copy_ask)
                .setPositiveButton(R.string.ok, (dialog, which) -> presenter.copySpoilerLink(post.getId(), spoilNumber))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}

