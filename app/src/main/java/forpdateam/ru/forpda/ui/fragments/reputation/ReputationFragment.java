package forpdateam.ru.forpda.ui.fragments.reputation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.Di;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.others.user.ForumUser;
import forpdateam.ru.forpda.api.reputation.Reputation;
import forpdateam.ru.forpda.api.reputation.models.RepData;
import forpdateam.ru.forpda.api.reputation.models.RepItem;
import forpdateam.ru.forpda.apirx.ForumUsersCache;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.presentation.reputation.ReputationPresenter;
import forpdateam.ru.forpda.presentation.reputation.ReputationView;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;
import forpdateam.ru.forpda.ui.views.ContentController;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.FunnyContent;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.pagination.PaginationHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 20.03.17.
 */

public class ReputationFragment extends RecyclerFragment implements ReputationView {

    @InjectPresenter
    ReputationPresenter presenter;

    @ProvidePresenter
    ReputationPresenter provideReputationPresenter() {
        return new ReputationPresenter(Di.get().reputationRepository);
    }

    private ReputationAdapter adapter;
    private PaginationHelper paginationHelper;
    private DynamicDialogMenu<ReputationFragment, RepItem> dialogMenu;

    private MenuItem descSortMenuItem;
    private MenuItem ascSortMenuItem;
    private MenuItem repModeMenuItem;
    private MenuItem upRepMenuItem;
    private MenuItem downRepMenuItem;

    public ReputationFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_reputation));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String url = getArguments().getString(ARG_TAB);
            if (url != null) {
                presenter.setCurrentData(Reputation.fromUrl(url));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        paginationHelper = new PaginationHelper(getActivity());
        paginationHelper.addInToolbar(inflater, toolbarLayout, configuration.isFitSystemWindow());
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewsReady();

        dialogMenu = new DynamicDialogMenu<>();
        dialogMenu.addItem(getString(R.string.profile), (context, data1) -> presenter.navigateToProfile(data1.getUserId()));
        dialogMenu.addItem(getString(R.string.go_to_message), (context, data1) -> presenter.navigateToMessage(data1));

        refreshLayout.setOnRefreshListener(() -> presenter.loadReputation());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ReputationAdapter();
        recyclerView.setAdapter(adapter);
        paginationHelper.setListener(paginationListener);
        adapter.setOnItemClickListener(adapterListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (paginationHelper != null)
            paginationHelper.destroy();
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        SubMenu subMenu = menu.addSubMenu(R.string.sorting_title);
        subMenu.getItem().setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        subMenu.getItem().setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_sort));
        descSortMenuItem = subMenu.add(R.string.sorting_desc).setOnMenuItemClickListener(menuItem -> {
            presenter.setSort(Reputation.SORT_DESC);
            return false;
        });
        ascSortMenuItem = subMenu.add(R.string.sorting_asc).setOnMenuItemClickListener(menuItem -> {
            presenter.setSort(Reputation.SORT_ASC);
            return false;
        });
        repModeMenuItem = menu.add(getString(presenter.getCurrentData().getMode().equals(Reputation.MODE_FROM) ? R.string.reputation_mode_from : R.string.reputation_mode_to))
                .setOnMenuItemClickListener(item -> {
                    presenter.changeReputationMode();
                    return false;
                });
        upRepMenuItem = menu.add(R.string.increase)
                .setOnMenuItemClickListener(item -> {
                    showChangeReputationDialog(true);
                    return false;
                });
        downRepMenuItem = menu.add(R.string.decrease)
                .setOnMenuItemClickListener(item -> {
                    showChangeReputationDialog(false);
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
            repModeMenuItem.setTitle(getString(presenter.getCurrentData().getMode().equals(Reputation.MODE_FROM) ? R.string.reputation_mode_from : R.string.reputation_mode_to));
            if (presenter.getCurrentData().getId() != ClientHelper.getUserId()) {
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

    public void showChangeReputationDialog(boolean type) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;

        View layout = inflater.inflate(R.layout.reputation_change_layout, null);
        assert layout != null;

        final TextView text = (TextView) layout.findViewById(R.id.reputation_text);
        final EditText messageField = (EditText) layout.findViewById(R.id.reputation_text_field);
        text.setText(String.format(getString(R.string.change_reputation_Type_Nick), getString(type ? R.string.increase : R.string.decrease), presenter.getCurrentData().getNick()));

        new AlertDialog.Builder(getContext())
                .setView(layout)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    presenter.changeReputation(type, messageField.getText().toString());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onChangeReputation(String result) {
        Toast.makeText(getContext(), result.isEmpty() ? getString(R.string.reputation_changed) : result, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean loadData() {
        if (!super.loadData()) {
            return false;
        }
        refreshToolbarMenuItems(false);
        presenter.loadReputation();
        return true;
    }

    private void tryShowAvatar(RepData repData) {
        toolbarImageView.setContentDescription(getString(R.string.user_avatar));
        toolbarImageView.setOnClickListener(view1 -> presenter.navigateToProfile(repData.getId()));
        if (repData.getNick() != null) {
            Observable.fromCallable(() -> ForumUsersCache.loadUserByNick(repData.getNick()))
                    .onErrorReturn(throwable -> new ForumUser())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(forumUser -> {
                        if (forumUser.getAvatar() != null && !forumUser.getAvatar().isEmpty()) {
                            ImageLoader.getInstance().displayImage(forumUser.getAvatar(), toolbarImageView);
                            toolbarImageView.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            toolbarImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void showReputation(RepData repData) {
        if (repData.getItems().isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                FunnyContent funnyContent = new FunnyContent(getContext())
                        .setImage(R.drawable.ic_history)
                        .setTitle(R.string.funny_reputation_nodata_title);
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA);
            }
            contentController.showContent(ContentController.TAG_NO_DATA);
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA);
        }

        tryShowAvatar(repData);

        adapter.addAll(repData.getItems());
        paginationHelper.updatePagination(repData.getPagination());
        refreshToolbarMenuItems(true);
        setSubtitle("" + (repData.getPositive() - repData.getNegative()) + " (+" + repData.getPositive() + " / -" + repData.getNegative() + ")");
        setTabTitle("Репутация " + repData.getNick() + (repData.getMode().equals(Reputation.MODE_FROM) ? ": кому изменял" : ""));
        setTitle("Репутация " + repData.getNick() + (repData.getMode().equals(Reputation.MODE_FROM) ? ": кому изменял" : ""));
        listScrollTop();
    }

    @Override
    public void showItemDialogMenu(RepItem item) {
        dialogMenu.disallowAll();
        dialogMenu.allow(0);
        if (item.getSourceUrl() != null)
            dialogMenu.allow(1);
        dialogMenu.show(getContext(), item.getUserNick(), ReputationFragment.this, item);
    }

    private PaginationHelper.PaginationListener paginationListener = new PaginationHelper.PaginationListener() {
        @Override
        public boolean onTabSelected(TabLayout.Tab tab) {
            return refreshLayout.isRefreshing();
        }

        @Override
        public void onSelectedPage(int pageNumber) {
            presenter.selectPage(pageNumber);
        }
    };

    private BaseAdapter.OnItemClickListener<RepItem> adapterListener = new BaseAdapter.OnItemClickListener<RepItem>() {
        @Override
        public void onItemClick(RepItem item) {
            presenter.onItemClick(item);
        }

        @Override
        public boolean onItemLongClick(RepItem item) {
            presenter.onItemLongClick(item);
            return false;
        }
    };
}
