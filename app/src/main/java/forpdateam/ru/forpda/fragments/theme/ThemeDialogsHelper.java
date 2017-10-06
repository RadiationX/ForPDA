package forpdateam.ru.forpda.fragments.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.IBaseForumPost;
import forpdateam.ru.forpda.api.search.models.SearchItem;
import forpdateam.ru.forpda.api.search.models.SearchSettings;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.fragments.jsinterfaces.IPostFunctions;
import forpdateam.ru.forpda.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.fragments.search.SearchFragment;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;
import io.reactivex.functions.Consumer;

/**
 * Created by radiationx on 01.11.16.
 */

public class ThemeDialogsHelper {
    private final static String reportWarningText = App.get().getString(R.string.report_warning);
    private static AlertDialogMenu<IPostFunctions, IBaseForumPost> userMenu, reputationMenu, postMenu;
    private static AlertDialogMenu<IPostFunctions, IBaseForumPost> showedUserMenu, showedReputationMenu, showedPostMenu;

    public static void showUserMenu(Context context, IPostFunctions theme, IBaseForumPost post) {
        if (userMenu == null) {
            userMenu = new AlertDialogMenu<>();
            showedUserMenu = new AlertDialogMenu<>();
            userMenu.addItem(App.get().getString(R.string.profile), (context1, data) -> IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=" + data.getUserId()));
            userMenu.addItem(App.get().getString(R.string.reputation), IPostFunctions::showReputationMenu);
            userMenu.addItem(App.get().getString(R.string.pm_qms), (context1, data) -> IntentHandler.handle("https://4pda.ru/forum/index.php?act=qms&amp;mid=" + data.getUserId()));
            userMenu.addItem(App.get().getString(R.string.user_themes), (context1, data) -> {
                SearchSettings settings = new SearchSettings();
                settings.setSource(SearchSettings.SOURCE_ALL.first);
                settings.setNick(data.getNick());
                settings.setResult(SearchSettings.RESULT_TOPICS.first);
                IntentHandler.handle(settings.toUrl());
            });
            userMenu.addItem(App.get().getString(R.string.messages_in_this_theme), (context1, data) -> {
                SearchSettings settings = new SearchSettings();
                settings.addForum(Integer.toString(data.getForumId()));
                settings.addTopic(Integer.toString(data.getTopicId()));
                settings.setSource(SearchSettings.SOURCE_CONTENT.first);
                settings.setNick(data.getNick());
                settings.setResult(SearchSettings.RESULT_POSTS.first);
                settings.setSubforums(SearchSettings.SUB_FORUMS_FALSE);
                IntentHandler.handle(settings.toUrl());
            });
            userMenu.addItem(App.get().getString(R.string.user_messages), (context1, data) -> {
                SearchSettings settings = new SearchSettings();
                settings.setSource(SearchSettings.SOURCE_CONTENT.first);
                settings.setNick(data.getNick());
                settings.setResult(SearchSettings.RESULT_POSTS.first);
                settings.setSubforums(SearchSettings.SUB_FORUMS_FALSE);
                IntentHandler.handle(settings.toUrl());
            });
        }
        showedUserMenu.clear();
        showedUserMenu.addItem(userMenu.get(0));
        showedUserMenu.addItem(userMenu.get(1));
        if (ClientHelper.getAuthState() == ClientHelper.AUTH_STATE_LOGIN && post.getUserId() != ClientHelper.getUserId())
            showedUserMenu.addItem(userMenu.get(2));
        showedUserMenu.addItem(userMenu.get(3));
        showedUserMenu.addItem(userMenu.get(4));
        showedUserMenu.addItem(userMenu.get(5));
        new AlertDialog.Builder(context)
                .setTitle(post.getNick())
                .setItems(showedUserMenu.getTitles(), (dialogInterface, i) -> showedUserMenu.onClick(i, theme, post))
                .show();
    }

    public static void showReputationMenu(Context context, IPostFunctions theme, IBaseForumPost post) {
        if (reputationMenu == null) {
            reputationMenu = new AlertDialogMenu<>();
            showedReputationMenu = new AlertDialogMenu<>();
            reputationMenu.addItem(App.get().getString(R.string.increase), (context1, data) -> context1.changeReputation(data, true));
            reputationMenu.addItem(App.get().getString(R.string.look), (context1, data) -> IntentHandler.handle("https://4pda.ru/forum/index.php?act=rep&view=history&amp;mid=" + data.getUserId()));
            reputationMenu.addItem(App.get().getString(R.string.decrease), (context1, data) -> context1.changeReputation(data, false));
        }
        showedReputationMenu.clear();
        if (ClientHelper.getAuthState() == ClientHelper.AUTH_STATE_LOGIN) {
            if (post.canPlusRep())
                showedReputationMenu.addItem(reputationMenu.get(0));
            showedReputationMenu.addItem(reputationMenu.get(1));
            if (post.canMinusRep())
                showedReputationMenu.addItem(reputationMenu.get(2));
        }
        new AlertDialog.Builder(context)
                .setTitle(App.get().getString(R.string.reputation) + " ".concat(post.getNick()))
                .setItems(showedReputationMenu.getTitles(), (dialogInterface, i) -> showedReputationMenu.onClick(i, theme, post))
                .show();
    }


    public static void showPostMenu(Context context, IPostFunctions theme, IBaseForumPost post) {
        if (postMenu == null) {
            postMenu = new AlertDialogMenu<>();
            showedPostMenu = new AlertDialogMenu<>();
            postMenu.addItem(App.get().getString(R.string.reply), IPostFunctions::reply);
            postMenu.addItem(App.get().getString(R.string.quote_from_clipboard), (context1, data) -> {
                String text = Utils.readFromClipboard();
                if (text != null && !text.isEmpty()) {
                    theme.quotePost(text, data);
                }
            });
            postMenu.addItem(App.get().getString(R.string.report), IPostFunctions::reportPost);
            postMenu.addItem(App.get().getString(R.string.edit), IPostFunctions::editPost);
            postMenu.addItem(App.get().getString(R.string.delete), IPostFunctions::deletePost);
            postMenu.addItem(App.get().getString(R.string.copy_link), (context1, data) -> {
                String url = "https://4pda.ru/forum/index.php?s=&showtopic=" + data.getTopicId() + "&view=findpost&p=" + data.getId();
                Utils.copyToClipBoard(url);
            });
            postMenu.addItem(App.get().getString(R.string.create_note), (context1, data) -> {
                String themeTitle = null;
                if (context1 instanceof SearchFragment) {
                    SearchItem searchItem = (SearchItem) data;
                    themeTitle = searchItem.getTitle();
                } else if (context1 instanceof ThemeFragment) {
                    ThemeFragment themeFragment = (ThemeFragment) context1;
                    themeTitle = themeFragment.currentPage.getTitle();
                }
                String title = String.format(App.get().getString(R.string.post_Topic_Nick_Number), themeTitle, data.getNick(), data.getId());
                String url = "https://4pda.ru/forum/index.php?s=&showtopic=" + data.getTopicId() + "&view=findpost&p=" + data.getId();
                NotesAddPopup.showAddNoteDialog(context, title, url);
            });
            postMenu.addItem(App.get().getString(R.string.share), (context12, data) -> {
                String url = "https://4pda.ru/forum/index.php?s=&showtopic=" + data.getTopicId() + "&view=findpost&p=" + data.getId();
                Utils.shareText(url);
            });
        }
        showedPostMenu.clear();
        if (ClientHelper.getAuthState() == ClientHelper.AUTH_STATE_LOGIN) {
            if (post.canQuote()) {
                showedPostMenu.addItem(postMenu.get(0));
                showedPostMenu.addItem(postMenu.get(1));
            }
            if (post.canReport())
                showedPostMenu.addItem(postMenu.get(2));
            if (post.canEdit())
                showedPostMenu.addItem(postMenu.get(3));
            if (post.canDelete())
                showedPostMenu.addItem(postMenu.get(4));
        }
        showedPostMenu.addItem(postMenu.get(5));
        showedPostMenu.addItem(postMenu.get(6));
        showedPostMenu.addItem(postMenu.get(7));
        new AlertDialog.Builder(context)
                .setItems(showedPostMenu.getTitles(), (dialogInterface, i) -> showedPostMenu.onClick(i, theme, post))
                .show();
    }

    public static void tryReportPost(Context context, IBaseForumPost post) {
        if (App.get().getPreferences().getBoolean("show_report_warning", true)) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.attention)
                    .setMessage(reportWarningText)
                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                        App.get().getPreferences().edit().putBoolean("show_report_warning", false).apply();
                        ThemeDialogsHelper.showReportDialog(context, post);
                    })
                    .show();
        } else {
            ThemeDialogsHelper.showReportDialog(context, post);
        }
    }

    @SuppressLint("InflateParams")
    public static void showReportDialog(Context context, IBaseForumPost post) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.report_layout, null);

        assert layout != null;
        final EditText messageField = (EditText) layout.findViewById(R.id.report_text_field);

        new AlertDialog.Builder(context)
                .setTitle(String.format(App.get().getString(R.string.report_to_post_Nick), post.getNick()))
                .setView(layout)
                .setPositiveButton(R.string.send, (dialogInterface, i) -> {
                    ThemeHelper.reportPost(s -> {
                        Toast.makeText(context, s.isEmpty() ? App.get().getString(R.string.unknown_error) : s, Toast.LENGTH_SHORT).show();
                    }, post.getTopicId(), post.getId(), messageField.getText().toString());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public static void deletePost(Context context, IBaseForumPost post, Consumer<Boolean> onNext) {
        new AlertDialog.Builder(context)
                .setMessage(String.format(App.get().getString(R.string.ask_delete_post_Nick), post.getNick()))
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    ThemeHelper.deletePost(b -> {
                        onNext.accept(b);
                        Toast.makeText(context, b ? App.get().getString(R.string.message_deleted) : App.get().getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }, post.getId());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @SuppressLint("InflateParams")
    public static void changeReputation(Context context, IBaseForumPost post, boolean type) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.reputation_change_layout, null);

        assert layout != null;
        final TextView text = (TextView) layout.findViewById(R.id.reputation_text);
        final EditText messageField = (EditText) layout.findViewById(R.id.reputation_text_field);
        text.setText(String.format(context.getString(R.string.change_reputation_Type_Nick), context.getString(type ? R.string.increase : R.string.decrease), post.getNick()));

        new AlertDialog.Builder(context)
                .setView(layout)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    ThemeHelper.changeReputation(s -> {
                        Toast.makeText(context, s.isEmpty() ? App.get().getString(R.string.reputation_changed) : s, Toast.LENGTH_SHORT).show();
                    }, post.getId(), post.getUserId(), type, messageField.getText().toString());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}

