package forpdateam.ru.forpda.ui.fragments.devdb;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.devdb.models.Brand;
import forpdateam.ru.forpda.api.devdb.models.DeviceSearch;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.devdb.adapters.BrandAdapter;
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.PauseOnScrollListener;
import forpdateam.ru.forpda.ui.views.messagepanel.AutoFitRecyclerView;

/**
 * Created by radiationx on 09.11.17.
 */

public class SearchFragment extends TabFragment implements BrandAdapter.OnItemClickListener<DeviceSearch.DeviceItem> {
    private BrandAdapter adapter;
    private SwipeRefreshLayout refreshLayout;
    private AutoFitRecyclerView recyclerView;
    private SearchView searchView;
    private MenuItem searchMenuItem;
    private String currentQuery = "";
    private DynamicDialogMenu<SearchFragment, DeviceSearch.DeviceItem> dialogMenu;

    public SearchFragment() {
        configuration.setDefaultTitle("Поиск устройств");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.fragment_brand);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
        recyclerView = (AutoFitRecyclerView) findViewById(R.id.base_list);
        contentController.setMainRefresh(refreshLayout);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewsReady();
        setCardsBackground();
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(this::loadData);

        PauseOnScrollListener pauseOnScrollListener = new PauseOnScrollListener(ImageLoader.getInstance(), true, true);
        recyclerView.addOnScrollListener(pauseOnScrollListener);

        adapter = new BrandAdapter();
        recyclerView.setColumnWidth(App.get().dpToPx(144));
        recyclerView.setAdapter(adapter);
        try {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addItemDecoration(new BrandFragment.SpacingItemDecoration(gridLayoutManager, App.px8));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        adapter.setItemClickListener(this);

        SearchManager searchManager = (SearchManager) getMainActivity().getSystemService(Context.SEARCH_SERVICE);
        if (null != searchManager) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getMainActivity().getComponentName()));
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setQueryHint(getString(R.string.search_keywords));

        LinearLayout searchEditFrame = (LinearLayout) searchView.findViewById(R.id.search_edit_frame);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) searchEditFrame.getLayoutParams();
        params.leftMargin = 0;

        View searchSrcText = searchView.findViewById(R.id.search_src_text);
        searchSrcText.setPadding(0, searchSrcText.getPaddingTop(), 0, searchSrcText.getPaddingBottom());

        searchMenuItem.expandActionView();
    }


    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        toolbar.inflateMenu(R.menu.qms_contacts_menu);
        searchMenuItem = getMenu().findItem(R.id.action_search);
        searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setIconifiedByDefault(true);
    }

    private void startSearch(String query) {
        this.currentQuery = query;
        loadData();
    }

    @Override
    public boolean loadData() {
        if (currentQuery != null && currentQuery.isEmpty()) {
            return false;
        }
        if (!super.loadData()) {
            return false;
        }
        setRefreshing(true);
        subscribe(RxApi.DevDb().search(currentQuery), this::onLoad, new Brand());
        return true;
    }

    private void onLoad(Brand brand) {
        setRefreshing(false);
        adapter.addAll(brand.getDevices());
        setTitle("Поиск " + currentQuery);
    }

    @Override
    public void onItemClick(DeviceSearch.DeviceItem item) {
        Bundle args = new Bundle();
        args.putString(DeviceFragment.ARG_DEVICE_ID, item.getId());
        TabManager.get().add(DeviceFragment.class, args);
    }

    @Override
    public boolean onItemLongClick(DeviceSearch.DeviceItem item) {
        if (dialogMenu == null) {
            dialogMenu = new DynamicDialogMenu<>();
            dialogMenu.addItem(getString(R.string.copy_link), (context, data) -> {
                Utils.copyToClipBoard("https://4pda.ru/devdb/" + data.getId());
            });
            dialogMenu.addItem(getString(R.string.share), (context, data) -> {
                Utils.shareText("https://4pda.ru/devdb/" + data.getId());
            });
            dialogMenu.addItem(getString(R.string.create_note), (context1, data) -> {
                String title = "DevDb: " + currentQuery;
                String url = "https://4pda.ru/devdb/" + data.getId();
                NotesAddPopup.showAddNoteDialog(context1.getContext(), title, url);
            });
        }
        dialogMenu.disallowAll();
        dialogMenu.allowAll();
        dialogMenu.show(getContext(), SearchFragment.this, item);
        return false;
    }
}
