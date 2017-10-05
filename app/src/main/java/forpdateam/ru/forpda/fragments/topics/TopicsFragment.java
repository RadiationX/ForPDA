package forpdateam.ru.forpda.fragments.topics;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.topcis.models.TopicItem;
import forpdateam.ru.forpda.api.topcis.models.TopicsData;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.fragments.RecyclerFragment;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.favorites.FavoritesHelper;
import forpdateam.ru.forpda.fragments.forum.ForumFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.pagination.PaginationHelper;

/**
 * Created by radiationx on 01.03.17.
 */

public class TopicsFragment extends RecyclerFragment implements TopicsAdapter.OnItemClickListener<TopicItem> {
    public final static String TOPICS_ID_ARG = "TOPICS_ID_ARG";
    private int id;
    private TopicsAdapter adapter;
    private Subscriber<TopicsData> mainSubscriber = new Subscriber<>(this);

    public TopicsFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_topics));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getInt(TOPICS_ID_ARG);
        }
    }


    private PaginationHelper paginationHelper;
    private int currentSt = 0;
    TopicsData data;
    private AlertDialogMenu<TopicsFragment, TopicItem> fullTopicsDialogMenu;
    private AlertDialogMenu<TopicsFragment, TopicItem> topicsDialogMenu;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewsReady();
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TopicsAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);

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
        return view;
    }


    @Override
    public void loadData() {
        super.loadData();
        setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Topics().getTopics(id, currentSt), this::onLoadThemes, new TopicsData(), v -> loadData());
    }

    private void onLoadThemes(TopicsData data) {
        setRefreshing(false);

        this.data = data;

        setTitle(data.getTitle());
        adapter.clear();
        if (!data.getForumItems().isEmpty())
            adapter.addSection(new Pair<>(getString(R.string.forum_section), data.getForumItems()));
        if (!data.getAnnounceItems().isEmpty())
            adapter.addSection(new Pair<>(getString(R.string.announce_section), data.getAnnounceItems()));
        if (!data.getPinnedItems().isEmpty())
            adapter.addSection(new Pair<>(getString(R.string.pinned_section), data.getPinnedItems()));
        adapter.addSection(new Pair<>(getString(R.string.themes_section), data.getTopicItems()));
        adapter.notifyDataSetChanged();
        paginationHelper.updatePagination(data.getPagination());
        setSubtitle(paginationHelper.getTitle());
        listScrollTop();
    }

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        getMenu()
                .add(R.string.open_forum)
                .setOnMenuItemClickListener(item -> {
                    Bundle args = new Bundle();
                    args.putInt(ForumFragment.ARG_FORUM_ID, id);
                    TabManager.getInstance().add(ForumFragment.class, args);
                    return true;
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        paginationHelper.destroy();
    }

    @Override
    public void onItemClick(TopicItem item) {
        if (item.isAnnounce()) return;
        if (item.isForum()) {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showforum=" + item.getId());
            return;
        }
        Bundle args = new Bundle();
        args.putString(TabFragment.ARG_TITLE, item.getTitle());
        IntentHandler.handle("https://4pda.ru/forum/index.php?showtopic=" + item.getId() + "&view=getnewpost", args);
    }

    @Override
    public boolean onItemLongClick(TopicItem item) {
        if (item.isAnnounce()) return false;
        if (fullTopicsDialogMenu == null) {
            fullTopicsDialogMenu = new AlertDialogMenu<>();
            topicsDialogMenu = new AlertDialogMenu<>();
            fullTopicsDialogMenu.addItem(getString(R.string.copy_link), (context, data1) -> Utils.copyToClipBoard("https://4pda.ru/forum/index.php?showtopic=".concat(Integer.toString(data1.getId()))));
            fullTopicsDialogMenu.addItem(getString(R.string.open_theme_forum), (context, data1) -> IntentHandler.handle("https://4pda.ru/forum/index.php?showforum=" + data.getId()));
            fullTopicsDialogMenu.addItem(getString(R.string.add_to_favorites), ((context, data1) -> {
                new AlertDialog.Builder(context.getContext())
                        .setItems(Favorites.SUB_NAMES, (dialog1, which1) -> {
                            FavoritesHelper.add(aBoolean -> {
                                Toast.makeText(getContext(), aBoolean ? getString(R.string.favorites_added) : getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
                            }, data1.getId(), Favorites.SUB_TYPES[which1]);
                        })
                        .show();
            }));
        }
        topicsDialogMenu.clear();
        topicsDialogMenu.addItem(fullTopicsDialogMenu.get(0));
        topicsDialogMenu.addItem(fullTopicsDialogMenu.get(1));
        if (ClientHelper.getAuthState()) {
            topicsDialogMenu.addItem(fullTopicsDialogMenu.get(2));
        }

        new AlertDialog.Builder(getContext())
                .setItems(topicsDialogMenu.getTitles(), (dialog, which) -> topicsDialogMenu.onClick(which, TopicsFragment.this, item))
                .show();
        return false;
    }
}
