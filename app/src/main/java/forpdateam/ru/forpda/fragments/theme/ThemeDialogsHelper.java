package forpdateam.ru.forpda.fragments.theme;

import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.search.models.SearchSettings;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;

/**
 * Created by radiationx on 01.11.16.
 */

class ThemeDialogsHelper {
    private static AlertDialogMenu<ThemeFragment, ThemePost> userMenu, reputationMenu, postMenu;
    private static AlertDialogMenu<ThemeFragment, ThemePost> showedUserMenu, showedReputationMenu, showedPostMenu;

    static void showUserMenu(ThemeFragment theme, ThemePost post) {
        if (userMenu == null) {
            userMenu = new AlertDialogMenu<>();
            showedUserMenu = new AlertDialogMenu<>();
            userMenu.addItem("Профиль", (context, data) -> IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=" + data.getUserId()));
            userMenu.addItem("Личные сообщения QMS", (context, data) -> IntentHandler.handle("http://4pda.ru/forum/index.php?act=qms&amp;mid=" + data.getUserId()));
            userMenu.addItem("Темы пользователя", (context, data) -> {
                SearchSettings settings = new SearchSettings();
                settings.setSource(SearchSettings.SOURCE_ALL.first);
                settings.setNick(data.getNick());
                settings.setResult(SearchSettings.RESULT_TOPICS.first);
                IntentHandler.handle(settings.toUrl());
            });
            userMenu.addItem("Сообщения в этой теме", (context, data) -> {
                SearchSettings settings = new SearchSettings();
                settings.addForum(context.currentPage.getForumId());
                settings.addTopic(context.currentPage.getId());
                settings.setSource(SearchSettings.SOURCE_CONTENT.first);
                settings.setNick(data.getNick());
                settings.setResult(SearchSettings.RESULT_POSTS.first);
                settings.setSubforums(SearchSettings.SUB_FORUMS_FALSE);
                IntentHandler.handle(settings.toUrl());
            });
            userMenu.addItem("Сообщения пользователя", (context, data) -> {
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
        if (Api.Auth().getState() && post.getUserId() != Api.Auth().getUserIdInt())
            showedUserMenu.addItem(userMenu.get(1));
        showedUserMenu.addItem(userMenu.get(2));
        showedUserMenu.addItem(userMenu.get(3));
        showedUserMenu.addItem(userMenu.get(4));
        new AlertDialog.Builder(theme.getContext())
                .setTitle(post.getNick())
                .setItems(showedUserMenu.getTitles(), (dialogInterface, i) -> showedUserMenu.onClick(i, theme, post))
                .show();
    }

    static void showReputationMenu(ThemeFragment theme, ThemePost post) {
        if (reputationMenu == null) {
            reputationMenu = new AlertDialogMenu<>();
            showedReputationMenu = new AlertDialogMenu<>();
            reputationMenu.addItem("Повысить", (context, data) -> context.changeReputation(data, true));
            reputationMenu.addItem("Посмотреть", (context, data) -> Toast.makeText(context.getContext(), "Слепой", Toast.LENGTH_SHORT).show());
            reputationMenu.addItem("Понизить", (context, data) -> context.changeReputation(data, false));
        }
        showedReputationMenu.clear();
        if (Api.Auth().getState()) {
            if (post.canPlusRep())
                showedReputationMenu.addItem(reputationMenu.get(0));
            showedReputationMenu.addItem(reputationMenu.get(1));
            if (post.canMinusRep())
                showedReputationMenu.addItem(reputationMenu.get(2));
        }
        new AlertDialog.Builder(theme.getContext())
                .setTitle("Репутация ".concat(post.getNick()))
                .setItems(showedReputationMenu.getTitles(), (dialogInterface, i) -> showedReputationMenu.onClick(i, theme, post))
                .show();
    }


    static void showPostMenu(ThemeFragment theme, ThemePost post) {
        if (postMenu == null) {
            postMenu = new AlertDialogMenu<>();
            showedPostMenu = new AlertDialogMenu<>();
            postMenu.addItem("Ответить", ThemeFragment::insertNick);
            postMenu.addItem("Пожаловаться", ThemeFragment::reportPost);
            postMenu.addItem("Изменить", ThemeFragment::editPost);
            postMenu.addItem("Удалить", ThemeFragment::deletePost);
            postMenu.addItem("Ссылка на сообщение", (context, data) -> Toast.makeText(context.getContext(), "Не умею", Toast.LENGTH_SHORT).show());
        }
        showedPostMenu.clear();
        if (Api.Auth().getState()) {
            if (theme.currentPage.canQuote())
                showedPostMenu.addItem(postMenu.get(0));
            if (post.canReport())
                showedPostMenu.addItem(postMenu.get(1));
            if (post.canEdit())
                showedPostMenu.addItem(postMenu.get(2));
            if (post.canDelete())
                showedPostMenu.addItem(postMenu.get(3));
        }
        showedPostMenu.addItem(postMenu.get(4));
        new AlertDialog.Builder(theme.getContext())
                .setItems(showedPostMenu.getTitles(), (dialogInterface, i) -> showedPostMenu.onClick(i, theme, post))
                .show();
    }
}

