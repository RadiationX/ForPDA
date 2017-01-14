package forpdateam.ru.forpda.fragments.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.fragments.theme.adapters.ThemePagesAdapter;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;

/**
 * Created by radiationx on 01.11.16.
 */

public class ThemeDialogsHelper {
    private static AlertDialogMenu<ThemeFragmentWeb, ThemePost> userMenu, reputationMenu, postMenu;
    private static AlertDialogMenu<ThemeFragmentWeb, ThemePost> showedUserMenu, showedReputationMenu, showedPostMenu;

    public static void showUserMenu(ThemeFragmentWeb theme, ThemePost post) {
        if (userMenu == null) {
            userMenu = new AlertDialogMenu<>();
            showedUserMenu = new AlertDialogMenu<>();
            userMenu.addItem("Профиль", (context, data) -> IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=" + data.getUserId()));
            userMenu.addItem("Личные сообщения QMS", (context, data) -> IntentHandler.handle("http://4pda.ru/forum/index.php?act=qms&mid=" + data.getUserId()));
            userMenu.addItem("Темы пользователя", (context, data) -> Toast.makeText(context.getContext(), "Не умею", Toast.LENGTH_SHORT).show());
            userMenu.addItem("Сообщения в этой теме", (context, data) -> Toast.makeText(context.getContext(), "Не умею", Toast.LENGTH_SHORT).show());
            userMenu.addItem("Сообщения пользователя", (context, data) -> Toast.makeText(context.getContext(), "Не умею", Toast.LENGTH_SHORT).show());
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

    public static void showReputationMenu(ThemeFragmentWeb theme, ThemePost post) {
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


    public static void showPostMenu(ThemeFragmentWeb theme, ThemePost post) {
        if (postMenu == null) {
            postMenu = new AlertDialogMenu<>();
            showedPostMenu = new AlertDialogMenu<>();
            postMenu.addItem("Ответить", (context, data) -> context.insertNick(data));
            postMenu.addItem("Пожаловаться", (context, data) -> context.insertNick(data));
            postMenu.addItem("Изменить", (context, data) -> context.insertNick(data));
            postMenu.addItem("Удалить", (context, data) -> context.insertNick(data));
            postMenu.addItem("Ссылка на сообщение", (context, data) -> Toast.makeText(context.getContext(), "Не умею", Toast.LENGTH_SHORT).show());
        }
        showedPostMenu.clear();
        if (Api.Auth().getState()) {
            if (theme.pageData.canQuote())
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

    public static void selectPage(ThemeFragmentWeb theme, ThemePage pageData) {
        final int[] pages = new int[pageData.getAllPagesCount()];

        for (int i = 0; i < pageData.getAllPagesCount(); i++)
            pages[i] = i + 1;

        LayoutInflater inflater = (LayoutInflater) theme.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.select_page_layout, null);

        assert view != null;
        final ListView listView = (ListView) view.findViewById(R.id.listview);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(new ThemePagesAdapter(theme.getContext(), pages));
        listView.setItemChecked(pageData.getCurrentPage() - 1, true);
        listView.setSelection(pageData.getCurrentPage() - 1);

        AlertDialog dialog = new AlertDialog.Builder(theme.getActivity())
                .setView(view)
                .show();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        listView.setOnItemClickListener((adapterView, view1, i2, l) -> {
            if (listView.getTag() != null && !((Boolean) listView.getTag())) {
                return;
            }
            theme.jumpToPage(i2 * pageData.getPostsOnPageCount());
            dialog.cancel();
        });
    }
}

