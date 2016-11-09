package forpdateam.ru.forpda.fragments.favorites;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.favorites.models.FavData;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.DividerItemDecoration;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;

/**
 * Created by radiationx on 22.09.16.
 */

public class FavoritesFragment extends TabFragment {
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private FavoritesAdapter.OnItemClickListener onItemClickListener =
            (view1, position, adapter1) -> {
                Bundle args = new Bundle();
                args.putString(TabFragment.TITLE_ARG, adapter1.getItem(position).getTopicTitle());
                IntentHandler.handle("http://4pda.ru/forum/index.php?showtopic=" + adapter1.getItem(position).getTopicId() + "&view=getnewpost", args);
            };
    private AlertDialogMenu<FavItem> favoriteDialogMenu;
    private FavoritesAdapter.OnLongItemClickListener onLongItemClickListener =
            (view1, position, adapter1) -> {
                FavItem favItem = adapter1.getItem(position);

                if (favoriteDialogMenu == null) {
                    favoriteDialogMenu = new AlertDialogMenu<>();
                    favoriteDialogMenu.addItem("Скопировать ссылку", data -> Utils.copyToClipBoard("http://4pda.ru/forum/index.php?showtopic=".concat(Integer.toString(data.getTopicId()))));
                    favoriteDialogMenu.addItem("Вложения", data -> IntentHandler.handle("http://4pda.ru/forum/index.php?act=attach&code=showtopic&tid=" + data.getTopicId()));
                    favoriteDialogMenu.addItem("Открыть форум темы", data -> IntentHandler.handle("http://4pda.ru/forum/index.php?showforum=" + data.getForumId()));
                    favoriteDialogMenu.addItem("Изменить тип подписки", data -> new AlertDialog.Builder(getContext())
                            .setItems(Favorites.SUB_NAMES, (dialog1, which1) -> changeFav(0, Favorites.SUB_TYPES[which1], data.getFavId()))
                            .show());
                    favoriteDialogMenu.addItem(getPinText(favItem.isPin()), data -> changeFav(1, data.isPin() ? "unpin" : "pin", data.getFavId()));
                    favoriteDialogMenu.addItem("Удалить", data -> changeFav(2, null, data.getFavId()));
                }

                int index = favoriteDialogMenu.containsIndex(getPinText(!favItem.isPin()));
                if (index != -1)
                    favoriteDialogMenu.changeTitle(index, getPinText(favItem.isPin()));

                new AlertDialog.Builder(getContext())
                        .setItems(favoriteDialogMenu.getTitles(), (dialog, which) -> {
                            Log.d("kek", "ocnlicl " + favItem + " : " + favItem.getFavId());
                            favoriteDialogMenu.onClick(which, favItem);
                        })
                        .show();
            };

    private Subscriber<FavData> mainSubscriber = new Subscriber<>();
    private Subscriber<Boolean> helperSubscriber = new Subscriber<>();

    private CharSequence getPinText(boolean b) {
        return b ? "Открепить" : "Закрепить";
    }

    @Override
    public String getDefaultTitle() {
        return "Избранное";
    }

    @Override
    public boolean isAlone() {
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        setWhiteBackground();
        baseInflateFragment(inflater, R.layout.fragment_qms_themes);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        recyclerView = (RecyclerView) findViewById(R.id.qms_list_themes);
        viewsReady();
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        return view;
    }

    @Override
    public void loadData() {
        if (refreshLayout != null)
            refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(Api.Favorites().get(), this::onLoadThemes, new FavData(), v -> loadData());
    }

    public void changeFav(int act, String type, int id) {
        helperSubscriber.subscribe(Api.Favorites().changeFav(act, type, id), this::onChangeFav, false);
    }

    private void onLoadThemes(FavData data) {
        FavoritesAdapter adapter = new FavoritesAdapter(data.getFavItems());
        adapter.setOnItemClickListener(onItemClickListener);
        adapter.setOnLongItemClickListener(onLongItemClickListener);
        recyclerView.setAdapter(adapter);
        refreshLayout.setRefreshing(false);
    }

    private void onChangeFav(boolean v) {
        if (!v)
            Toast.makeText(getContext(), "При выполнении операции произошла ошибка", Toast.LENGTH_SHORT).show();
        loadData();
    }
}
