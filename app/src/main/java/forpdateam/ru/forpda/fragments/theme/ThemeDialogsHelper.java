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
import forpdateam.ru.forpda.api.search.models.SearchSettings;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.fragments.jsinterfaces.IPostFunctions;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;

/**
 * Created by radiationx on 01.11.16.
 */

public class ThemeDialogsHelper {
    private final static String reportWarningText = "Вам не нужно указывать здесь тему и сообщение, модератор автоматически получит эту информацию.\n\n" +
            "Пожалуйста, используйте эту возможность форума только для жалоб о некорректном сообщении!\n" +
            "Для связи с модератором используйте личные сообщения.";
    private static AlertDialogMenu<IPostFunctions, IBaseForumPost> userMenu, reputationMenu, postMenu;
    private static AlertDialogMenu<IPostFunctions, IBaseForumPost> showedUserMenu, showedReputationMenu, showedPostMenu;

    public static void showUserMenu(Context context, IPostFunctions theme, IBaseForumPost post) {
        if (userMenu == null) {
            userMenu = new AlertDialogMenu<>();
            showedUserMenu = new AlertDialogMenu<>();
            userMenu.addItem("Профиль", (context1, data) -> IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=" + data.getUserId()));
            userMenu.addItem("Личные сообщения QMS", (context1, data) -> IntentHandler.handle("http://4pda.ru/forum/index.php?act=qms&amp;mid=" + data.getUserId()));
            userMenu.addItem("Темы пользователя", (context1, data) -> {
                SearchSettings settings = new SearchSettings();
                settings.setSource(SearchSettings.SOURCE_ALL.first);
                settings.setNick(data.getNick());
                settings.setResult(SearchSettings.RESULT_TOPICS.first);
                IntentHandler.handle(settings.toUrl());
            });
            userMenu.addItem("Сообщения в этой теме", (context1, data) -> {
                SearchSettings settings = new SearchSettings();
                settings.addForum(data.getForumId());
                settings.addTopic(data.getTopicId());
                settings.setSource(SearchSettings.SOURCE_CONTENT.first);
                settings.setNick(data.getNick());
                settings.setResult(SearchSettings.RESULT_POSTS.first);
                settings.setSubforums(SearchSettings.SUB_FORUMS_FALSE);
                IntentHandler.handle(settings.toUrl());
            });
            userMenu.addItem("Сообщения пользователя", (context1, data) -> {
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
        if (ClientHelper.getAuthState() == ClientHelper.AUTH_STATE_LOGIN && post.getUserId() != ClientHelper.getUserId())
            showedUserMenu.addItem(userMenu.get(1));
        showedUserMenu.addItem(userMenu.get(2));
        showedUserMenu.addItem(userMenu.get(3));
        showedUserMenu.addItem(userMenu.get(4));
        new AlertDialog.Builder(context)
                .setTitle(post.getNick())
                .setItems(showedUserMenu.getTitles(), (dialogInterface, i) -> showedUserMenu.onClick(i, theme, post))
                .show();
    }

    public static void showReputationMenu(Context context, IPostFunctions theme, IBaseForumPost post) {
        if (reputationMenu == null) {
            reputationMenu = new AlertDialogMenu<>();
            showedReputationMenu = new AlertDialogMenu<>();
            reputationMenu.addItem("Повысить", (context1, data) -> context1.changeReputation(data, true));
            reputationMenu.addItem("Посмотреть", (context1, data) -> IntentHandler.handle("http://4pda.ru/forum/index.php?act=rep&view=history&amp;mid=" + data.getUserId()));
            reputationMenu.addItem("Понизить", (context1, data) -> context1.changeReputation(data, false));
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
                .setTitle("Репутация ".concat(post.getNick()))
                .setItems(showedReputationMenu.getTitles(), (dialogInterface, i) -> showedReputationMenu.onClick(i, theme, post))
                .show();
    }


    public static void showPostMenu(Context context, IPostFunctions theme, IBaseForumPost post) {
        if (postMenu == null) {
            postMenu = new AlertDialogMenu<>();
            showedPostMenu = new AlertDialogMenu<>();
            postMenu.addItem("Ответить", IPostFunctions::insertNick);
            postMenu.addItem("Пожаловаться", IPostFunctions::reportPost);
            postMenu.addItem("Изменить", IPostFunctions::editPost);
            postMenu.addItem("Удалить", IPostFunctions::deletePost);
            postMenu.addItem("Скопировать ссылку", (context1, data) -> {
                String url = "http://4pda.ru/forum/index.php?s=&showtopic=" + data.getTopicId() + "&view=findpost&p=" + data.getId();
                Utils.copyToClipBoard(url);
            });
        }
        showedPostMenu.clear();
        if (ClientHelper.getAuthState() == ClientHelper.AUTH_STATE_LOGIN) {
            if (post.canQuote())
                showedPostMenu.addItem(postMenu.get(0));
            if (post.canReport())
                showedPostMenu.addItem(postMenu.get(1));
            if (post.canEdit())
                showedPostMenu.addItem(postMenu.get(2));
            if (post.canDelete())
                showedPostMenu.addItem(postMenu.get(3));
        }
        showedPostMenu.addItem(postMenu.get(4));
        new AlertDialog.Builder(context)
                .setItems(showedPostMenu.getTitles(), (dialogInterface, i) -> showedPostMenu.onClick(i, theme, post))
                .show();
    }

    public static void tryReportPost(Context context, IBaseForumPost post) {
        if (App.getInstance().getPreferences().getBoolean("show_report_warning", true)) {
            new AlertDialog.Builder(context)
                    .setTitle("Внимание!")
                    .setMessage(reportWarningText)
                    .setPositiveButton("Ок", (dialogInterface, i) -> {
                        App.getInstance().getPreferences().edit().putBoolean("show_report_warning", false).apply();
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
                .setTitle("Жалоба на пост ".concat(post.getNick()))
                .setView(layout)
                .setPositiveButton("Отправить", (dialogInterface, i) -> {
                    ThemeHelper.reportPost(s -> {
                        Toast.makeText(context, s.isEmpty() ? "Неизвестная ошибка" : s, Toast.LENGTH_SHORT).show();
                    }, post.getForumId(), post.getTopicId(), messageField.getText().toString());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    public static void deletePost(Context context, IBaseForumPost post) {
        new AlertDialog.Builder(context)
                .setMessage("Удалить пост ".concat(post.getNick()).concat(" ?"))
                .setPositiveButton("Да", (dialogInterface, i) -> {
                    ThemeHelper.deletePost(s -> {
                        Toast.makeText(context, !s.isEmpty() ? "Сообщение удалено" : "Ошибка", Toast.LENGTH_SHORT).show();
                    }, post.getId());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @SuppressLint("InflateParams")
    public static void changeReputation(Context context, IBaseForumPost post, boolean type) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.reputation_change_layout, null);

        assert layout != null;
        final TextView text = (TextView) layout.findViewById(R.id.reputation_text);
        final EditText messageField = (EditText) layout.findViewById(R.id.reputation_text_field);
        text.setText((type ? "Повысить" : "Понизить").concat(" репутацию ").concat(post.getNick()).concat(" ?"));

        new AlertDialog.Builder(context)
                .setView(layout)
                .setPositiveButton("Да", (dialogInterface, i) -> {
                    ThemeHelper.changeReputation(s -> {
                        Toast.makeText(context, s.isEmpty() ? "Репутация изменена" : s, Toast.LENGTH_SHORT).show();
                    }, post.getId(), post.getUserId(), type, messageField.getText().toString());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}

