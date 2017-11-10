package forpdateam.ru.forpda.ui.fragments.topics;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.topcis.models.TopicItem;
import forpdateam.ru.forpda.api.topcis.models.TopicsData;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.common.rx.Subscriber;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesHelper;
import forpdateam.ru.forpda.ui.fragments.forum.ForumFragment;
import forpdateam.ru.forpda.ui.fragments.forum.ForumHelper;
import forpdateam.ru.forpda.ui.fragments.search.SearchFragment;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.pagination.PaginationHelper;

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
    private DynamicDialogMenu<TopicsFragment, TopicItem> dialogMenu;

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
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TopicsAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);

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
    }

    @Override
    public boolean loadData() {
        if (!super.loadData()) {
            return false;
        }
        setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Topics().getTopics(id, currentSt), this::onLoadThemes, new TopicsData(), v -> loadData());
        return true;
    }

    private void onLoadThemes(TopicsData data) {
        setRefreshing(false);

        this.data = data;

        setTitle(data.getTitle());
        refreshList();
        paginationHelper.updatePagination(data.getPagination());
        setSubtitle(paginationHelper.getTitle());
        listScrollTop();
    }

    private void refreshList() {
        adapter.clear();
        if (!data.getForumItems().isEmpty())
            adapter.addSection(new Pair<>(getString(R.string.forum_section), data.getForumItems()));
        if (!data.getAnnounceItems().isEmpty())
            adapter.addSection(new Pair<>(getString(R.string.announce_section), data.getAnnounceItems()));
        if (!data.getPinnedItems().isEmpty())
            adapter.addSection(new Pair<>(getString(R.string.pinned_section), data.getPinnedItems()));
        adapter.addSection(new Pair<>(getString(R.string.themes_section), data.getTopicItems()));
        adapter.notifyDataSetChanged();
    }

    public void markRead(int topicId) {
        Log.d("SUKA", "markRead " + topicId);
        for (TopicItem item : data.getTopicItems()) {
            if (item.getId() == topicId) {
                item.setNew(false);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        getMenu()
                .add(R.string.open_forum)
                .setOnMenuItemClickListener(item -> {
                    Bundle args = new Bundle();
                    args.putInt(ForumFragment.ARG_FORUM_ID, id);
                    TabManager.get().add(ForumFragment.class, args);
                    return true;
                });
        if (ClientHelper.getAuthState()) {
            getMenu()
                    .add(R.string.mark_read)
                    .setOnMenuItemClickListener(item -> {
                        new AlertDialog.Builder(getContext())
                                .setMessage(getString(R.string.mark_read) + "?")
                                .setPositiveButton(R.string.ok, (dialog, which) -> ForumHelper.markRead(o -> Toast.makeText(getContext(), R.string.action_complete, Toast.LENGTH_SHORT).show(), id))
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                        return true;
                    });
        }


        getMenu().add(R.string.fragment_title_search)
                .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_search))
                .setOnMenuItemClickListener(item -> {
                    String url = "https://4pda.ru/forum/index.php?act=search&source=all&forums%5B%5D=" + id;
                    Bundle args = new Bundle();
                    args.putString(TabFragment.ARG_TAB, url);
                    TabManager.get().add(SearchFragment.class, args);
                    return true;
                })
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (paginationHelper != null)
            paginationHelper.destroy();
    }

    @Override
    public void onItemClick(TopicItem item) {
        if (item.isAnnounce()) {
            Bundle args = new Bundle();
            args.putString(TabFragment.ARG_TITLE, item.getTitle());
            IntentHandler.handle(item.getAnnounceUrl(), args);
            return;
        }
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
        if (dialogMenu == null) {
            dialogMenu = new DynamicDialogMenu<>();
            dialogMenu.addItem(getString(R.string.copy_link), (context, data1) -> {
                String url;
                if (item.isAnnounce()) {
                    url = item.getAnnounceUrl();
                } else {
                    url = "https://4pda.ru/forum/index.php?showtopic=" + data1.getId();
                }
                Utils.copyToClipBoard(url);
            });
            dialogMenu.addItem(getString(R.string.open_theme_forum), (context, data1) -> IntentHandler.handle("https://4pda.ru/forum/index.php?showforum=" + data.getId()));
            dialogMenu.addItem(getString(R.string.add_to_favorites), ((context, data1) -> {
                if (data1.isForum()) {
                    FavoritesHelper.addForumWithDialog(getContext(), aBoolean -> {
                        Toast.makeText(getContext(), aBoolean ? getString(R.string.favorites_added) : getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
                    }, data1.getId());
                } else {
                    FavoritesHelper.addWithDialog(getContext(), aBoolean -> {
                        Toast.makeText(getContext(), aBoolean ? getString(R.string.favorites_added) : getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
                    }, data1.getId());
                }

            }));
        }
        dialogMenu.disallowAll();
        dialogMenu.allow(0);
        if (!item.isAnnounce()) {
            dialogMenu.allow(1);
            if (ClientHelper.getAuthState()) {
                dialogMenu.allow(2);
            }
        }
        dialogMenu.show(getContext(), TopicsFragment.this, item);
        return false;
    }
}
