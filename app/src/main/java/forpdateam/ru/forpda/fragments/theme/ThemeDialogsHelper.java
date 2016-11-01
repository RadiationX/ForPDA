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
import forpdateam.ru.forpda.fragments.theme.adapters.ThemePagesAdapter;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;

/**
 * Created by radiationx on 01.11.16.
 */

public class ThemeDialogsHelper {
    private static AlertDialogMenu<ThemePost> userMenu, reputationMenu, postMenu;

    public static void showUserMenu(ThemeFragmentWeb theme, ThemePost post) {
        if (userMenu == null) {
            userMenu = new AlertDialogMenu<>();
            userMenu.addItem("Профиль", data -> IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=" + data.getUserId()));
            if (Api.Auth().getState())
                userMenu.addItem("Личные сообщения QMS", data -> IntentHandler.handle("http://4pda.ru/forum/index.php?act=qms&mid=" + data.getUserId()));
            userMenu.addItem("Темы пользователя", data -> Toast.makeText(theme.getContext(), "Не умею", Toast.LENGTH_SHORT).show());
            userMenu.addItem("Сообщения в этой теме", data -> Toast.makeText(theme.getContext(), "Не умею", Toast.LENGTH_SHORT).show());
            userMenu.addItem("Сообщения пользователя", data -> Toast.makeText(theme.getContext(), "Не умею", Toast.LENGTH_SHORT).show());
        }
        new AlertDialog.Builder(theme.getContext())
                .setTitle(post.getNick())
                .setItems(userMenu.getTitles(), (dialogInterface, i) -> userMenu.onClick(i, post))
                .show();
    }

    public static void showReputationMenu(ThemeFragmentWeb theme, ThemePost post) {
        if (reputationMenu == null) {
            reputationMenu = new AlertDialogMenu<>();
            reputationMenu.addItem("Посмотреть", data -> Toast.makeText(theme.getContext(), "Слепой", Toast.LENGTH_SHORT).show());
        }
        if (Api.Auth().getState()) {
            int index = reputationMenu.containsIndex("Повысить");
            if (index == -1) {
                if (post.canPlusRep())
                    reputationMenu.addItem(0, "Повысить", data -> theme.changeReputation(post, true));
            } else {
                if (!post.canPlusRep())
                    reputationMenu.remove(index);
            }

            index = reputationMenu.containsIndex("Понизить");
            if (index == -1) {
                if (post.canPlusRep())
                    reputationMenu.addItem(2, "Понизить", data -> theme.changeReputation(post, false));
            } else {
                if (!post.canPlusRep())
                    reputationMenu.remove(index);
            }
        }
        new AlertDialog.Builder(theme.getContext())
                .setTitle("Репутация ".concat(post.getNick()))
                .setItems(reputationMenu.getTitles(), (dialogInterface, i) -> reputationMenu.onClick(i, post))
                .show();
    }

    public static void showPostMenu(ThemeFragmentWeb theme, ThemePost post) {
        if (postMenu == null) {
            postMenu = new AlertDialogMenu<>();
            if (Api.Auth().getState()) {
                if (theme.pageData.canQuote()) {
                    postMenu.addItem("Ответить", data -> theme.insertNick(post));
                }
                if (post.canReport()) {
                    postMenu.addItem("Пожаловаться", data -> theme.reportPost(post));
                }
            }
            postMenu.addItem("Ссылка на сообщение", data -> Toast.makeText(theme.getContext(), "Не умею", Toast.LENGTH_SHORT).show());
        }
        new AlertDialog.Builder(theme.getContext())
                .setItems(postMenu.getTitles(), (dialogInterface, i) -> postMenu.onClick(i, post))
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

