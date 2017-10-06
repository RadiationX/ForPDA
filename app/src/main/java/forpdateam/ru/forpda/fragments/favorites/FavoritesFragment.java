package forpdateam.ru.forpda.fragments.favorites;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.events.models.NotificationEvent;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.favorites.Sorting;
import forpdateam.ru.forpda.api.favorites.interfaces.IFavItem;
import forpdateam.ru.forpda.api.favorites.models.FavData;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.data.models.TabNotification;
import forpdateam.ru.forpda.data.realm.favorites.FavItemBd;
import forpdateam.ru.forpda.fragments.RecyclerFragment;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.forum.ForumHelper;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.settings.Preferences;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.ContentController;
import forpdateam.ru.forpda.views.FunnyContent;
import forpdateam.ru.forpda.views.pagination.PaginationHelper;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 22.09.16.
 */

public class FavoritesFragment extends RecyclerFragment implements FavoritesAdapter.OnItemClickListener<IFavItem> {
    private AlertDialogMenu<FavoritesFragment, IFavItem> favoriteDialogMenu, showedFavoriteDialogMenu;
    private Realm realm;
    private FavoritesAdapter adapter;
    private Subscriber<FavData> mainSubscriber = new Subscriber<>(this);
    boolean markedRead = false;

    private boolean unreadTop = Preferences.Lists.Topic.isUnreadTop();
    private boolean loadAll = Preferences.Lists.Favorites.isLoadAll();
    private Observer favoritesPreferenceObserver = (observable, o) -> {
        if (o == null) return;
        String key = (String) o;
        switch (key) {
            case Preferences.Lists.Topic.UNREAD_TOP: {
                boolean newUnreadTop = Preferences.Lists.Topic.isUnreadTop();
                if (newUnreadTop != unreadTop) {
                    unreadTop = newUnreadTop;
                    bindView();
                }
                break;
            }
            case Preferences.Lists.Topic.SHOW_DOT: {
                boolean newShowDot = Preferences.Lists.Topic.isShowDot();
                if (newShowDot != adapter.isShowDot()) {
                    adapter.setShowDot(newShowDot);
                    adapter.notifyDataSetChanged();
                }
                break;
            }
            case Preferences.Lists.Favorites.LOAD_ALL: {
                loadAll = Preferences.Lists.Favorites.isLoadAll();
                break;
            }
        }
    };

    private Observer notification = (observable, o) -> {
        if (o == null) return;
        TabNotification event = (TabNotification) o;
        runInUiThread(() -> handleEvent(event));
    };

    public FavoritesFragment() {
        configuration.setAlone(true);
        //configuration.setUseCache(true);
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_favorite));
    }

    private CharSequence getPinText(boolean b) {
        return getString(b ? R.string.fav_unpin : R.string.fav_pin);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    private PaginationHelper paginationHelper;
    private int currentSt = 0;

    private BottomSheetDialog dialog;
    private ViewGroup sortingView;
    private Spinner keySpinner;
    private Spinner orderSpinner;
    private Button sortApply;
    private Sorting sorting = new Sorting(Preferences.Lists.Favorites.getSortingKey(), Preferences.Lists.Favorites.getSortingOrder());

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        sortingView = (ViewGroup) View.inflate(getContext(), R.layout.favorite_sorting, null);
        keySpinner = (Spinner) sortingView.findViewById(R.id.sorting_key);
        orderSpinner = (Spinner) sortingView.findViewById(R.id.sorting_order);
        sortApply = (Button) sortingView.findViewById(R.id.sorting_apply);
        dialog = new BottomSheetDialog(getContext());


        contentController.setFirstLoad(false);
        viewsReady();
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        adapter = new FavoritesAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        paginationHelper = new PaginationHelper(getActivity());
        paginationHelper.addInToolbar(inflater, toolbarLayout);
        //paginationHelper.addInList(inflater, listContainer);
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

        setItems(keySpinner, new String[]{getString(R.string.fav_sort_last_post), getString(R.string.fav_sort_title)}, 0);
        setItems(orderSpinner, new String[]{getString(R.string.sorting_asc), getString(R.string.sorting_desc)}, 0);
        selectSpinners(sorting);
        sortApply.setOnClickListener(v -> {
            switch (keySpinner.getSelectedItemPosition()) {
                case 0:
                    sorting.setKey(Sorting.Key.LAST_POST);
                    break;
                case 1:
                    sorting.setKey(Sorting.Key.TITLE);
                    break;
            }
            switch (orderSpinner.getSelectedItemPosition()) {
                case 0:
                    sorting.setOrder(Sorting.Order.ASC);
                    break;
                case 1:
                    sorting.setOrder(Sorting.Order.DESC);
                    break;
            }
            Preferences.Lists.Favorites.setSortingKey(sorting.getKey());
            Preferences.Lists.Favorites.setSortingOrder(sorting.getOrder());
            loadData();
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        });

        bindView();
        App.get().addPreferenceChangeObserver(favoritesPreferenceObserver);
        App.get().subscribeFavorites(notification);
        return view;
    }

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        getMenu().add(R.string.mark_all_read)
                .setOnMenuItemClickListener(item -> {
                    new AlertDialog.Builder(getContext())
                            .setMessage(App.get().getString(R.string.mark_all_read) + "?")
                            .setPositiveButton(R.string.ok, (dialog, which) -> {
                                ForumHelper.markAllRead(o -> {
                                    loadData();
                                });
                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                    return false;
                });

        getMenu().add(R.string.sorting_title)
                .setIcon(R.drawable.ic_toolbar_sort).setOnMenuItemClickListener(menuItem -> {
            hidePopupWindows();
            if (sortingView != null && sortingView.getParent() != null && sortingView.getParent() instanceof ViewGroup) {
                ((ViewGroup) sortingView.getParent()).removeView(sortingView);
            }
            if (sortingView != null) {
                dialog.setContentView(sortingView);
                dialog.show();
            }
            return false;
        });
    }

    @Override
    public void loadData() {
        super.loadData();
        setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Favorites().getFavorites(currentSt, loadAll, sorting), this::onLoadThemes, new FavData(), v -> loadData());
    }

    private void onLoadThemes(FavData data) {
        setRefreshing(false);


        sorting = data.getSorting();
        selectSpinners(sorting);
        switch (data.getSorting().getKey()) {
            case Sorting.Key.LAST_POST:
                keySpinner.setSelection(0);
                break;
            case Sorting.Key.TITLE:
                keySpinner.setSelection(1);
                break;
        }
        switch (data.getSorting().getOrder()) {
            case Sorting.Order.ASC:
                orderSpinner.setSelection(0);
                break;
            case Sorting.Order.DESC:
                orderSpinner.setSelection(1);
                break;
        }

        if (realm.isClosed()) return;
        realm.executeTransactionAsync(r -> {
            r.delete(FavItemBd.class);
            List<FavItemBd> bdList = new ArrayList<>();
            for (FavItem item : data.getItems()) {
                bdList.add(new FavItemBd(item));
            }
            r.copyToRealmOrUpdate(bdList);
            bdList.clear();
        }, this::bindView);
        paginationHelper.updatePagination(data.getPagination());
        setSubtitle(paginationHelper.getTitle());
        listScrollTop();

    }

    private void selectSpinners(Sorting sorting) {
        switch (sorting.getKey()) {
            case Sorting.Key.LAST_POST:
                keySpinner.setSelection(0);
                break;
            case Sorting.Key.TITLE:
                keySpinner.setSelection(1);
                break;
        }
        switch (sorting.getOrder()) {
            case Sorting.Order.ASC:
                orderSpinner.setSelection(0);
                break;
            case Sorting.Order.DESC:
                orderSpinner.setSelection(1);
                break;
        }
    }

    private void setItems(Spinner spinner, String[] items, int selection) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getMainActivity(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(selection);
        //spinner.setOnItemSelectedListener(listener);
    }

    private void bindView() {
        //setRefreshing(false);
        if (realm.isClosed()) return;
        RealmResults<FavItemBd> results = realm.where(FavItemBd.class).findAll();

        if (results.isEmpty()) {
            if(!contentController.contains(ContentController.TAG_NO_DATA)){
                FunnyContent funnyContent = new FunnyContent(getContext())
                        .setImage(R.drawable.ic_star)
                        .setTitle(R.string.funny_favorites_nodata_title)
                        .setDesc(R.string.funny_favorites_nodata_desc);
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA);
            }
            contentController.showContent(ContentController.TAG_NO_DATA);
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA);
        }

        ArrayList<IFavItem> nonBdResult = new ArrayList<>();
        for (FavItemBd itemBd : results) {
            nonBdResult.add(new FavItem(itemBd));
        }
        refreshList(nonBdResult);
    }

    private void refreshList(Collection<IFavItem> newList) {
        ArrayList<IFavItem> pinnedUnread = new ArrayList<>();
        ArrayList<IFavItem> itemsUnread = new ArrayList<>();
        ArrayList<IFavItem> pinned = new ArrayList<>();
        ArrayList<IFavItem> items = new ArrayList<>();
        for (IFavItem item : newList) {
            if (item.isPin()) {
                if (unreadTop && item.isNewMessages()) {
                    pinnedUnread.add(item);
                } else {
                    pinned.add(item);
                }
            } else {
                if (unreadTop && item.isNewMessages()) {
                    itemsUnread.add(item);
                } else {
                    items.add(item);
                }
            }
        }

        adapter.clear();
        if (!pinnedUnread.isEmpty()) {
            adapter.addSection(new Pair<>(getString(R.string.fav_unreaded_pinned), pinnedUnread));
        }
        if (!itemsUnread.isEmpty()) {
            adapter.addSection(new Pair<>(getString(R.string.fav_unreaded), itemsUnread));
        }
        if (!pinned.isEmpty()) {
            adapter.addSection(new Pair<>(getString(R.string.fav_pinned), pinned));
        }
        adapter.addSection(new Pair<>(getString(R.string.fav_themes), items));
        adapter.notifyDataSetChanged();
        if (!Client.getInstance().getNetworkState()) {
            ClientHelper.getInstance().notifyCountsChanged();
        }
    }

    private void handleEvent(TabNotification event) {
        if (event.isWebSocket()) return;
        if (realm.isClosed()) return;
        RealmResults<FavItemBd> results = realm.where(FavItemBd.class).findAll();
        ArrayList<IFavItem> currentItems = new ArrayList<>();
        for (FavItemBd itemBd : results) {
            currentItems.add(new FavItem(itemBd));
        }

        //for (int i = event.getLoadedEvents().size() - 1; i >= 0; i--) {
        //NotificationEvent loadedEvent = event.getLoadedEvents().get(i);
        NotificationEvent loadedEvent = event.getEvent();
        int id = loadedEvent.getSourceId();
        for (IFavItem item : currentItems) {
            if (item.getTopicId() == id) {
                currentItems.remove(item);
                item.setNewMessages(true);
                item.setLastUserNick(loadedEvent.getUserNick());
                item.setLastUserId(loadedEvent.getUserId());
                item.setPin(loadedEvent.isImportant());
                //Collections.swap(currentItems, currentItems.indexOf(item), 0);
                currentItems.add(0, item);
                break;
            }
        }
        //}

        if (realm.isClosed()) return;
        realm.executeTransactionAsync(r -> {
            r.delete(FavItemBd.class);
            List<FavItemBd> bdList = new ArrayList<>();
            for (IFavItem item : currentItems) {
                bdList.add(new FavItemBd(item));
            }
            r.copyToRealmOrUpdate(bdList);
            bdList.clear();
        }, this::bindView);
    }

    public void changeFav(int action, String type, int favId) {
        FavoritesHelper.changeFav(this::onChangeFav, action, favId, -1, type);
    }

    public void markRead(int topicId) {
        Log.d("SUKA", "markRead " + topicId);
        realm.executeTransactionAsync(realm1 -> {
            IFavItem favItem = realm1.where(FavItemBd.class).equalTo("topicId", topicId).findFirst();
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
            markedRead = false;
            bindView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.get().removePreferenceChangeObserver(favoritesPreferenceObserver);
        App.get().unSubscribeFavorites(notification);
        paginationHelper.destroy();
        realm.close();
    }

    private void onChangeFav(boolean v) {
        /*if (!v)
            Toast.makeText(getContext(), "При выполнении операции произошла ошибка", Toast.LENGTH_SHORT).show();*/
        Toast.makeText(getContext(), R.string.action_complete, Toast.LENGTH_SHORT).show();
        loadData();
    }

    @Override
    public void onItemClick(IFavItem item) {
        Bundle args = new Bundle();
        args.putString(TabFragment.ARG_TITLE, item.getTopicTitle());
        if (item.isForum()) {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showforum=" + item.getForumId(), args);
        } else {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showtopic=" + item.getTopicId() + "&view=getnewpost", args);
        }
    }

    @Override
    public boolean onItemLongClick(IFavItem item) {
        if (favoriteDialogMenu == null) {
            favoriteDialogMenu = new AlertDialogMenu<>();
            showedFavoriteDialogMenu = new AlertDialogMenu<>();
            favoriteDialogMenu.addItem(getString(R.string.copy_link), (context, data) -> {
                if (data.isForum()) {
                    Utils.copyToClipBoard("https://4pda.ru/forum/index.php?showforum=".concat(Integer.toString(data.getForumId())));
                } else {
                    Utils.copyToClipBoard("https://4pda.ru/forum/index.php?showtopic=".concat(Integer.toString(data.getTopicId())));
                }
            });
            favoriteDialogMenu.addItem(getString(R.string.attachments), (context, data) -> IntentHandler.handle("https://4pda.ru/forum/index.php?act=attach&code=showtopic&tid=" + data.getTopicId()));
            favoriteDialogMenu.addItem(getString(R.string.open_theme_forum), (context, data) -> IntentHandler.handle("https://4pda.ru/forum/index.php?showforum=" + data.getForumId()));
            favoriteDialogMenu.addItem(getString(R.string.fav_change_subscribe_type), (context, data) -> {
                new AlertDialog.Builder(context.getContext())
                        .setTitle(R.string.favorites_subscribe_email)
                        .setItems(Favorites.SUB_NAMES, (dialog1, which1) -> context.changeFav(Favorites.ACTION_EDIT_SUB_TYPE, Favorites.SUB_TYPES[which1], data.getFavId()))
                        .show();
            });
            favoriteDialogMenu.addItem(getPinText(item.isPin()), (context, data) -> context.changeFav(Favorites.ACTION_EDIT_PIN_STATE, data.isPin() ? "unpin" : "pin", data.getFavId()));
            favoriteDialogMenu.addItem(getString(R.string.delete), (context, data) -> context.changeFav(Favorites.ACTION_DELETE, null, data.getFavId()));
        }
        showedFavoriteDialogMenu.clear();

        showedFavoriteDialogMenu.addItem(favoriteDialogMenu.get(0));
        if (!item.isForum()) {
            showedFavoriteDialogMenu.addItem(favoriteDialogMenu.get(1));
            showedFavoriteDialogMenu.addItem(favoriteDialogMenu.get(2));
        }
        showedFavoriteDialogMenu.addItem(favoriteDialogMenu.get(3));
        showedFavoriteDialogMenu.addItem(favoriteDialogMenu.get(4));
        showedFavoriteDialogMenu.addItem(favoriteDialogMenu.get(5));

        int index = showedFavoriteDialogMenu.containsIndex(getPinText(!item.isPin()));
        if (index != -1)
            showedFavoriteDialogMenu.changeTitle(index, getPinText(item.isPin()));

        new AlertDialog.Builder(getContext())
                .setItems(showedFavoriteDialogMenu.getTitles(), (dialog, which) -> {
                    showedFavoriteDialogMenu.onClick(which, FavoritesFragment.this, item);
                })
                .show();
        return false;
    }
}
