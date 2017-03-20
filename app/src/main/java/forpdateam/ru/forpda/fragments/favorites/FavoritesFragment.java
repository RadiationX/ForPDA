package forpdateam.ru.forpda.fragments.favorites;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
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
import forpdateam.ru.forpda.pagination.PaginationHelper;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 22.09.16.
 */

public class FavoritesFragment extends TabFragment {
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private FavoritesAdapter.OnItemClickListener onItemClickListener =
            favItem -> {
                Bundle args = new Bundle();
                args.putString(TabFragment.TITLE_ARG, favItem.getTopicTitle());
                IntentHandler.handle("http://4pda.ru/forum/index.php?showtopic=" + favItem.getTopicId() + "&view=getnewpost", args);
            };
    private AlertDialogMenu<FavoritesFragment, FavItem> favoriteDialogMenu;
    private FavoritesAdapter.OnLongItemClickListener onLongItemClickListener =
            favItem -> {
                if (favoriteDialogMenu == null) {
                    favoriteDialogMenu = new AlertDialogMenu<>();
                    favoriteDialogMenu.addItem("Скопировать ссылку", (context, data) -> Utils.copyToClipBoard("http://4pda.ru/forum/index.php?showtopic=".concat(Integer.toString(data.getTopicId()))));
                    favoriteDialogMenu.addItem("Вложения", (context, data) -> IntentHandler.handle("http://4pda.ru/forum/index.php?act=attach&code=showtopic&tid=" + data.getTopicId()));
                    favoriteDialogMenu.addItem("Открыть форум темы", (context, data) -> IntentHandler.handle("http://4pda.ru/forum/index.php?showforum=" + data.getForumId()));
                    favoriteDialogMenu.addItem("Изменить тип подписки", (context, data) -> {
                        new AlertDialog.Builder(context.getContext())
                                .setItems(Favorites.SUB_NAMES, (dialog1, which1) -> context.changeFav(Favorites.ACTION_CHANGE_SUB_TYPE, Favorites.SUB_TYPES[which1], data.getFavId()))
                                .show();
                    });
                    favoriteDialogMenu.addItem(getPinText(favItem.isPin()), (context, data) -> context.changeFav(Favorites.ACTION_CHANGE_PIN_STATE, data.isPin() ? "unpin" : "pin", data.getFavId()));
                    favoriteDialogMenu.addItem("Удалить", (context, data) -> context.changeFav(Favorites.ACTION_DELETE, null, data.getFavId()));
                }

                int index = favoriteDialogMenu.containsIndex(getPinText(!favItem.isPin()));
                if (index != -1)
                    favoriteDialogMenu.changeTitle(index, getPinText(favItem.isPin()));

                new AlertDialog.Builder(getContext())
                        .setItems(favoriteDialogMenu.getTitles(), (dialog, which) -> {
                            Log.d("kek", "ocnlicl " + favItem + " : " + favItem.getFavId());
                            favoriteDialogMenu.onClick(which, FavoritesFragment.this, favItem);
                        })
                        .show();
            };

    private Realm realm;
    private RealmResults<FavItem> results;
    private FavoritesAdapter adapter;
    private Subscriber<FavData> mainSubscriber = new Subscriber<>(this);
    boolean markedRead = false;
    private FavData data;

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

    @Override
    public boolean isUseCache() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    private PaginationHelper paginationHelper = new PaginationHelper();
    private int currentSt = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        setWhiteBackground();
        baseInflateFragment(inflater, R.layout.fragment_qms_themes);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        recyclerView = (RecyclerView) findViewById(R.id.qms_list_themes);
        viewsReady();
        loadData();
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        adapter = new FavoritesAdapter();
        adapter.setOnItemClickListener(onItemClickListener);
        adapter.setOnLongItemClickListener(onLongItemClickListener);
        recyclerView.setAdapter(adapter);

        paginationHelper.inflatePagination(getContext(), inflater, toolbar);
        paginationHelper.setupToolbar((CollapsingToolbarLayout) findViewById(R.id.toolbar_layout));
        paginationHelper.setListener(new PaginationHelper.PaginationListener() {
            @Override
            public boolean onTabSelected(TabLayout.Tab tab) {
                return refreshLayout.isRefreshing();
            }

            @Override
            public void onSelectedPage(int pageNumber) {
                currentSt = pageNumber;
                loadData();
            }
        });

        bindView();
        return view;
    }

    @Override
    public void loadData() {
        if (refreshLayout != null)
            refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(Api.Favorites().get(currentSt), this::onLoadThemes, new FavData(), v -> loadData());
    }

    private void onLoadThemes(FavData data) {
        Log.d("kek", "loaded itms " + data.getItems().size() + " : " + results.size());
        if (refreshLayout != null)
            refreshLayout.setRefreshing(false);

        this.data = data;
        if (data.getItems().size() == 0)
            return;

        realm.executeTransactionAsync(r -> {
            r.delete(FavItem.class);
            r.copyToRealmOrUpdate(data.getItems());
        }, this::bindView);
        paginationHelper.updatePagination(data.getPagination());
        setSubtitle(paginationHelper.getString());
    }

    private void bindView() {
        results = realm.where(FavItem.class).findAll();
        if (results.size() != 0) {
            adapter.addAll(results);
        }
        Api.get().notifyObservers();
    }

    public void changeFav(int action, String type, int favId) {
        FavoritesHelper.changeFav(this::onChangeFav, action, favId, -1,type);
    }

    public void markRead(int topicId) {
        realm.executeTransactionAsync(realm1 -> {
            FavItem favItem = realm1.where(FavItem.class).equalTo("topicId", topicId).findFirst();
            if (favItem != null) {
                favItem.setNewMessages(false);
            }
        });
        markedRead = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (markedRead) {
            bindView();
            markedRead = false;
        }
    }

    private void onChangeFav(boolean v) {
        if (!v)
            Toast.makeText(getContext(), "При выполнении операции произошла ошибка", Toast.LENGTH_SHORT).show();
        loadData();
    }
}
