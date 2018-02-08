package forpdateam.ru.forpda.ui.fragments.favorites;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.Di;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.favorites.Sorting;
import forpdateam.ru.forpda.api.favorites.models.FavData;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.common.Preferences;
import forpdateam.ru.forpda.entity.app.TabNotification;
import forpdateam.ru.forpda.presentation.favorites.FavoritesPresenter;
import forpdateam.ru.forpda.presentation.favorites.FavoritesView;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;
import forpdateam.ru.forpda.ui.fragments.forum.ForumHelper;
import forpdateam.ru.forpda.ui.views.ContentController;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.FunnyContent;
import forpdateam.ru.forpda.ui.views.adapters.BaseSectionedAdapter;
import forpdateam.ru.forpda.ui.views.pagination.PaginationHelper;

/**
 * Created by radiationx on 22.09.16.
 */

public class FavoritesFragment extends RecyclerFragment implements FavoritesView {
    public final static CharSequence[] SUB_NAMES = {
            App.get().getString(R.string.fav_subscribe_none),
            App.get().getString(R.string.fav_subscribe_delayed),
            App.get().getString(R.string.fav_subscribe_immediate),
            App.get().getString(R.string.fav_subscribe_daily),
            App.get().getString(R.string.fav_subscribe_weekly),
            App.get().getString(R.string.fav_subscribe_pinned)};

    @InjectPresenter
    FavoritesPresenter presenter;

    @ProvidePresenter
    FavoritesPresenter provideFavoritesPresenter() {
        return new FavoritesPresenter(Di.get().favoritesRepository);
    }

    private DynamicDialogMenu<FavoritesFragment, FavItem> dialogMenu;
    private FavoritesAdapter adapter;

    private boolean unreadTop = false;
    private boolean loadAll = false;
    private PaginationHelper paginationHelper;
    private int currentSt = 0;

    private BottomSheetDialog dialog;
    private ViewGroup sortingView;
    private Spinner keySpinner;
    private Spinner orderSpinner;
    private Button sortApply;
    private Sorting sorting;
    private Observer favoritesPreferenceObserver = (observable, o) -> {
        if (o == null) return;
        String key = (String) o;
        switch (key) {
            case Preferences.Lists.Topic.UNREAD_TOP: {
                boolean newUnreadTop = Preferences.Lists.Topic.isUnreadTop(getContext());
                if (newUnreadTop != unreadTop) {
                    unreadTop = newUnreadTop;
                    presenter.showFavorites();
                }
                break;
            }
            case Preferences.Lists.Topic.SHOW_DOT: {
                boolean newShowDot = Preferences.Lists.Topic.isShowDot(getContext());
                if (newShowDot != adapter.isShowDot()) {
                    adapter.setShowDot(newShowDot);
                    adapter.notifyDataSetChanged();
                }
                break;
            }
            case Preferences.Lists.Favorites.LOAD_ALL: {
                loadAll = Preferences.Lists.Favorites.isLoadAll(getContext());
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
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_favorite));
    }

    private CharSequence getPinText(boolean b) {
        return getString(b ? R.string.fav_unpin : R.string.fav_pin);
    }

    private CharSequence getSubText(int subTypeIndex) {
        return String.format("%s (%s)", getString(R.string.fav_change_subscribe_type), SUB_NAMES[subTypeIndex]);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        unreadTop = Preferences.Lists.Topic.isUnreadTop(context);
        loadAll = Preferences.Lists.Favorites.isLoadAll(context);
        sorting = new Sorting(Preferences.Lists.Favorites.getSortingKey(context), Preferences.Lists.Favorites.getSortingOrder(context));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        sortingView = (ViewGroup) View.inflate(getContext(), R.layout.favorite_sorting, null);
        keySpinner = (Spinner) sortingView.findViewById(R.id.sorting_key);
        orderSpinner = (Spinner) sortingView.findViewById(R.id.sorting_order);
        sortApply = (Button) sortingView.findViewById(R.id.sorting_apply);
        dialog = new BottomSheetDialog(getContext());
        paginationHelper = new PaginationHelper(getActivity());
        paginationHelper.addInToolbar(inflater, toolbarLayout, configuration.isFitSystemWindow());
        contentController.setFirstLoad(false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewsReady();

        dialogMenu = new DynamicDialogMenu<>();
        dialogMenu.addItem(getString(R.string.copy_link), (context, data) -> presenter.copyLink(data));
        dialogMenu.addItem(getString(R.string.attachments), (context, data) -> presenter.openAttachments(data));
        dialogMenu.addItem(getString(R.string.open_theme_forum), (context, data) -> presenter.openForum(data));
        dialogMenu.addItem(getString(R.string.fav_change_subscribe_type), (context, data) -> presenter.showSubscribeDialog(data));
        dialogMenu.addItem(getPinText(false), (context, data) -> presenter.changeFav(Favorites.ACTION_EDIT_PIN_STATE, data.isPin() ? "unpin" : "pin", data.getFavId()));
        dialogMenu.addItem(getString(R.string.delete), (context, data) -> presenter.changeFav(Favorites.ACTION_DELETE, null, data.getFavId()));

        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoritesAdapter();
        adapter.setOnItemClickListener(adapterListener);
        recyclerView.setAdapter(adapter);

        paginationHelper.setListener(paginationListener);

        initSpinnerItems(keySpinner, new String[]{getString(R.string.fav_sort_last_post), getString(R.string.fav_sort_title)});
        initSpinnerItems(orderSpinner, new String[]{getString(R.string.sorting_asc), getString(R.string.sorting_desc)});
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
            Preferences.Lists.Favorites.setSortingKey(getContext(), sorting.getKey());
            Preferences.Lists.Favorites.setSortingOrder(getContext(), sorting.getOrder());
            loadData();
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        });

        presenter.showFavorites();
        App.get().addPreferenceChangeObserver(favoritesPreferenceObserver);
        App.get().subscribeFavorites(notification);
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        menu.add(R.string.mark_all_read)
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

        menu.add(R.string.sorting_title)
                .setIcon(R.drawable.ic_toolbar_sort)
                .setOnMenuItemClickListener(menuItem -> {
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
    public boolean loadData() {
        if (!super.loadData()) {
            return false;
        }
        presenter.getFavorites(currentSt, loadAll, sorting);
        return true;
    }

    @Override
    public void onLoadFavorites(FavData data) {
        presenter.saveFavorites(data.getItems());
        sorting = data.getSorting();
        selectSpinners(sorting);
        paginationHelper.updatePagination(data.getPagination());
        setSubtitle(paginationHelper.getTitle());
    }

    @Override
    public void onShowFavorite(List<FavItem> items) {
        if (items.isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
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
        ArrayList<FavItem> pinnedUnread = new ArrayList<>();
        ArrayList<FavItem> itemsUnread = new ArrayList<>();
        ArrayList<FavItem> pinned = new ArrayList<>();
        ArrayList<FavItem> otherItems = new ArrayList<>();
        for (FavItem item : items) {
            if (item.isPin()) {
                if (unreadTop && item.isNew()) {
                    pinnedUnread.add(item);
                } else {
                    pinned.add(item);
                }
            } else {
                if (unreadTop && item.isNew()) {
                    itemsUnread.add(item);
                } else {
                    otherItems.add(item);
                }
            }
        }

        adapter.clear();
        if (!pinnedUnread.isEmpty()) {
            adapter.addSection(getString(R.string.fav_unreaded_pinned), pinnedUnread);
        }
        if (!itemsUnread.isEmpty()) {
            adapter.addSection(getString(R.string.fav_unreaded), itemsUnread);
        }
        if (!pinned.isEmpty()) {
            adapter.addSection(getString(R.string.fav_pinned), pinned);
        }
        adapter.addSection(getString(R.string.fav_themes), otherItems);
        adapter.notifyDataSetChanged();
        if (!ClientHelper.getNetworkState(getContext())) {
            ClientHelper.get().notifyCountsChanged();
        }
    }

    @Override
    public void onHandleEvent(int count) {
        ClientHelper.setFavoritesCount(count);
        ClientHelper.get().notifyCountsChanged();
        presenter.showFavorites();
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

    private void initSpinnerItems(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getMainActivity(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
    }

    private void handleEvent(TabNotification event) {
        if (!Preferences.Notifications.Favorites.isLiveTab(getContext())) return;
        presenter.handleEvent(event, sorting, ClientHelper.getFavoritesCount());
    }

    @Override
    public void changeFav(int action, String type, int favId) {
        FavoritesHelper.changeFav(this::onChangeFav, action, favId, -1, type);
    }

    public void markRead(int topicId) {
        presenter.markRead(topicId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.get().removePreferenceChangeObserver(favoritesPreferenceObserver);
        App.get().unSubscribeFavorites(notification);
        if (paginationHelper != null)
            paginationHelper.destroy();
    }

    private void onChangeFav(boolean v) {
        Toast.makeText(getContext(), R.string.action_complete, Toast.LENGTH_SHORT).show();
        loadData();
    }

    @Override
    public void showSubscribeDialog(FavItem item) {
        int subTypeIndex = Arrays.asList(Favorites.SUB_TYPES).indexOf(item.getSubType());
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.favorites_subscribe_email)
                .setSingleChoiceItems(FavoritesFragment.SUB_NAMES, subTypeIndex, (dialog, which1) -> {
                    presenter.changeFav(Favorites.ACTION_EDIT_SUB_TYPE, Favorites.SUB_TYPES[which1], item.getFavId());
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void showItemDialogMenu(FavItem item) {
        dialogMenu.disallowAll();
        dialogMenu.allow(0);
        if (!item.isForum()) {
            dialogMenu.allow(1);
            dialogMenu.allow(2);
        }
        dialogMenu.allow(3);
        dialogMenu.allow(4);
        dialogMenu.allow(5);

        int index = dialogMenu.containsIndex(getPinText(!item.isPin()));
        if (index != -1)
            dialogMenu.changeTitle(index, getPinText(item.isPin()));

        int subTypeIndex = Arrays.asList(Favorites.SUB_TYPES).indexOf(item.getSubType());
        dialogMenu.changeTitle(3, getSubText(subTypeIndex));

        dialogMenu.show(getContext(), FavoritesFragment.this, item);
    }

    private PaginationHelper.PaginationListener paginationListener = new PaginationHelper.PaginationListener() {
        @Override
        public boolean onTabSelected(TabLayout.Tab tab) {
            return refreshLayout.isRefreshing();
        }

        @Override
        public void onSelectedPage(int pageNumber) {
            currentSt = pageNumber;
            loadData();
        }
    };

    private BaseSectionedAdapter.OnItemClickListener<FavItem> adapterListener = new BaseSectionedAdapter.OnItemClickListener<FavItem>() {
        @Override
        public void onItemClick(FavItem item) {
            presenter.onItemClick(item);
        }

        @Override
        public boolean onItemLongClick(FavItem item) {
            presenter.onItemLongClick(item);
            return false;
        }
    };
}
