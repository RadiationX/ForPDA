package forpdateam.ru.forpda.fragments.reputation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.reputation.Reputation;
import forpdateam.ru.forpda.api.reputation.models.RepData;
import forpdateam.ru.forpda.api.reputation.models.RepItem;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.fragments.RecyclerFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.ContentController;
import forpdateam.ru.forpda.views.FunnyContent;
import forpdateam.ru.forpda.views.pagination.PaginationHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 20.03.17.
 */

public class ReputationFragment extends RecyclerFragment implements ReputationAdapter.OnItemClickListener<RepItem> {
    private ReputationAdapter adapter;
    private Subscriber<RepData> mainSubscriber = new Subscriber<>(this);
    private PaginationHelper paginationHelper;
    private RepData data = new RepData();
    private static AlertDialogMenu<ReputationFragment, RepItem> repDialogMenu;
    private AlertDialogMenu<ReputationFragment, RepItem> showedRepDialogMenu;


    public ReputationFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_reputation));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String url = getArguments().getString(ARG_TAB);
            if (url != null) {
                data = Reputation.fromUrl(data, url);
            }
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewsReady();
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ReputationAdapter();
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
                data.getPagination().setSt(pageNumber);
                loadData();
            }
        });

        adapter.setOnItemClickListener(this);

        return view;
    }

    private void someClick(RepItem item) {
        if (repDialogMenu == null) {
            repDialogMenu = new AlertDialogMenu<>();
            repDialogMenu.addItem(getString(R.string.profile), (context, data1) -> {
                IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=" + data1.getUserId());
            });
            repDialogMenu.addItem(getString(R.string.go_to_message), (context, data1) -> {
                IntentHandler.handle(data1.getSourceUrl());
            });
        }
        if (showedRepDialogMenu == null)
            showedRepDialogMenu = new AlertDialogMenu<>();

        showedRepDialogMenu.clear();
        showedRepDialogMenu.addItem(repDialogMenu.get(0));
        if (item.getSourceUrl() != null)
            showedRepDialogMenu.addItem(repDialogMenu.get(1));

        new AlertDialog.Builder(getContext())
                .setTitle(item.getUserNick())
                .setItems(showedRepDialogMenu.getTitles(), (dialog, which) -> showedRepDialogMenu.onClick(which, ReputationFragment.this, item))
                .show();
    }

    private MenuItem descSortMenuItem;
    private MenuItem ascSortMenuItem;
    private MenuItem repModeMenuItem;
    private MenuItem upRepMenuItem;
    private MenuItem downRepMenuItem;

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        SubMenu subMenu = getMenu().addSubMenu(R.string.sorting_title);
        subMenu.getItem().setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        subMenu.getItem().setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_sort));
        descSortMenuItem = subMenu.add(R.string.sorting_desc).setOnMenuItemClickListener(menuItem -> {
            data.setSort(Reputation.SORT_DESC);
            loadData();
            return false;
        });
        ascSortMenuItem = subMenu.add(R.string.sorting_asc).setOnMenuItemClickListener(menuItem -> {
            data.setSort(Reputation.SORT_ASC);
            loadData();
            return false;
        });
        repModeMenuItem = getMenu().add(getString(data.getMode().equals(Reputation.MODE_FROM) ? R.string.reputation_mode_from : R.string.reputation_mode_to))
                .setOnMenuItemClickListener(item -> {
                    if (data.getMode().equals(Reputation.MODE_FROM))
                        data.setMode(Reputation.MODE_TO);
                    else
                        data.setMode(Reputation.MODE_FROM);
                    loadData();
                    return false;
                });
        upRepMenuItem = getMenu().add(R.string.increase)
                .setOnMenuItemClickListener(item -> {
                    changeReputation(true);
                    return false;
                });
        downRepMenuItem = getMenu().add(R.string.decrease)
                .setOnMenuItemClickListener(item -> {
                    changeReputation(false);
                    return false;
                });
        refreshToolbarMenuItems(false);
    }

    @Override
    protected void refreshToolbarMenuItems(boolean enable) {
        super.refreshToolbarMenuItems(enable);
        if (enable) {
            descSortMenuItem.setEnabled(true);
            ascSortMenuItem.setEnabled(true);
            repModeMenuItem.setEnabled(true);
            repModeMenuItem.setTitle(getString(data.getMode().equals(Reputation.MODE_FROM) ? R.string.reputation_mode_from : R.string.reputation_mode_to));
            if (data.getId() != ClientHelper.getUserId()) {
                upRepMenuItem.setEnabled(true);
                upRepMenuItem.setVisible(true);
                downRepMenuItem.setEnabled(true);
                downRepMenuItem.setVisible(true);
            }
        } else {
            descSortMenuItem.setEnabled(false);
            ascSortMenuItem.setEnabled(false);
            repModeMenuItem.setEnabled(false);
            upRepMenuItem.setEnabled(false);
            upRepMenuItem.setEnabled(false);
            upRepMenuItem.setVisible(false);
            downRepMenuItem.setVisible(false);
        }
    }

    public void changeReputation(boolean type) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.reputation_change_layout, null);

        assert layout != null;
        final TextView text = (TextView) layout.findViewById(R.id.reputation_text);
        final EditText messageField = (EditText) layout.findViewById(R.id.reputation_text_field);
        text.setText(String.format(getString(R.string.change_reputation_Type_Nick), getString(type ? R.string.increase : R.string.decrease), data.getNick()));

        new AlertDialog.Builder(getContext())
                .setView(layout)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    changeReputation(s -> {
                        Toast.makeText(getContext(), s.isEmpty() ? getString(R.string.reputation_changed) : s, Toast.LENGTH_SHORT).show();
                        loadData();
                    }, 0, data.getId(), type, messageField.getText().toString());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void changeReputation(@NonNull Consumer<String> onNext, int postId, int userId, boolean type, String message) {
        RxApi.Reputation().editReputation(postId, userId, type, message).onErrorReturn(throwable -> "error")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext);
    }

    @Override
    public void loadData() {
        super.loadData();
        setRefreshing(true);
        refreshToolbarMenuItems(false);
        mainSubscriber.subscribe(RxApi.Reputation().getReputation(data), this::onLoadThemes, data, v -> loadData());
    }

    private void onLoadThemes(RepData data) {
        setRefreshing(false);

        if (data.getItems().isEmpty()) {
            if(!contentController.contains(ContentController.TAG_NO_DATA)){
                FunnyContent funnyContent = new FunnyContent(getContext())
                        .setImage(R.drawable.ic_history)
                        .setTitle(R.string.funny_reputation_nodata_title);
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA);
            }
            contentController.showContent(ContentController.TAG_NO_DATA);
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA);
        }

        this.data = data;


        adapter.addAll(data.getItems());
        paginationHelper.updatePagination(data.getPagination());
        refreshToolbarMenuItems(true);
        //setSubtitle(paginationHelper.getString());
        setSubtitle("" + (data.getPositive() - data.getNegative()) + " (+" + data.getPositive() + " / -" + data.getNegative() + ")");
        setTabTitle("Репутация " + data.getNick() + (data.getMode().equals(Reputation.MODE_FROM) ? ": кому изменял" : ""));
        setTitle("Репутация " + data.getNick() + (data.getMode().equals(Reputation.MODE_FROM) ? ": кому изменял" : ""));
        listScrollTop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        paginationHelper.destroy();
    }

    @Override
    public void onItemClick(RepItem item) {
        someClick(item);
    }

    @Override
    public boolean onItemLongClick(RepItem item) {
        someClick(item);
        return false;
    }
}
