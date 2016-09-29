package forpdateam.ru.forpda.fragments.favorites;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.favorites.models.FavData;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.utils.DividerItemDecoration;
import forpdateam.ru.forpda.utils.ErrorHandler;
import forpdateam.ru.forpda.utils.IntentHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by radiationx on 22.09.16.
 */

public class FavoritesFragment extends TabFragment {
    public final static String defaultTitle = "Избранное";
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private FavoritesAdapter.OnItemClickListener onItemClickListener =
            (view1, position, adapter1) -> {
                IntentHandler.handle("http://4pda.ru/forum/index.php?showtopic="+adapter1.getItem(position).getTopicId()+"&view=getlastpost");
            };
    private FavoritesAdapter.OnLongItemClickListener onLongItemClickListener =
            (view1, position, adapter1) -> {
                CharSequence[] items = {"Скопировать ссылку", "Вложения", "Открыть форум темы", "Изменить тип подписки", adapter1.getItem(position).isPin() ? "Открепить" : "Закрепить", "Удалить"};
                new AlertDialog.Builder(getContext())
                        .setItems(items, (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    ClipboardManager clipboard = (ClipboardManager) getMainActivity().getSystemService(CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("What? Label?", "http://4pda.ru/forum/index.php?showtopic=" + adapter1.getItem(position).getTopicId());
                                    clipboard.setPrimaryClip(clip);
                                    break;
                                case 1:
                                    IntentHandler.handle("http://4pda.ru/forum/index.php?act=attach&code=showtopic&tid=" + adapter1.getItem(position).getTopicId());
                                    break;
                                case 2:
                                    IntentHandler.handle("http://4pda.ru/forum/index.php?showforum=" + adapter1.getItem(position).getForumId());
                                    break;
                                case 3:
                                    new AlertDialog.Builder(getContext())
                                            .setItems(Favorites.SUB_NAMES, (dialog1, which1) -> changeFav(0, Favorites.SUB_TYPES[which1], adapter1.getItem(position).getFavId()))
                                            .show();
                                    break;
                                case 4:
                                    changeFav(1, adapter1.getItem(position).isPin() ? "unpin" : "pin", adapter1.getItem(position).getFavId());
                                    break;
                                case 5:
                                    changeFav(2, null, adapter1.getItem(position).getFavId());
                                    break;
                            }
                        })
                        .show();
            };

    @Override
    public String getDefaultTitle() {
        return defaultTitle;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        setWhiteBackground();
        inflater.inflate(R.layout.fragment_qms_themes, (ViewGroup) view.findViewById(R.id.fragment_content), true);
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
        getCompositeDisposable().add(Api.Favorites().get()
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, view1 -> loadData());
                    return new FavData();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onLoadThemes, throwable -> {
                    Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    public void changeFav(int act, String type, int id) {
        getCompositeDisposable().add(Api.Favorites().changeFav(act, type, id)
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, view1 -> loadData());
                    return null;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onChangeFav, throwable -> {
                    Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void onLoadThemes(FavData data) {
        adapter = new FavoritesAdapter(data.getFavItems());
        adapter.setOnItemClickListener(onItemClickListener);
        adapter.setOnLongItemClickListener(onLongItemClickListener);
        recyclerView.setAdapter(adapter);
        refreshLayout.setRefreshing(false);
    }

    private void onChangeFav(Void v) {
        loadData();
    }
}
