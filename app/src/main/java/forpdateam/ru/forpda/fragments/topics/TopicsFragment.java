package forpdateam.ru.forpda.fragments.topics;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.topcis.models.TopicItem;
import forpdateam.ru.forpda.api.topcis.models.TopicsData;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.favorites.FavoritesHelper;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.pagination.PaginationHelper;

/**
 * Created by radiationx on 01.03.17.
 */

public class TopicsFragment extends TabFragment {
    public final static String TOPICS_ID_ARG = "TOPICS_ID_ARG";
    private int id;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private TopicsAdapter adapter;
    private Subscriber<TopicsData> mainSubscriber = new Subscriber<>(this);

    public TopicsFragment() {
        configuration.setDefaultTitle("Темы форума");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getInt(TOPICS_ID_ARG);
        }
    }


    private PaginationHelper paginationHelper = new PaginationHelper();
    private int currentSt = 0;
    TopicsData data;
    private AlertDialogMenu<TopicsFragment, TopicItem> topicsDialogMenu;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setWhiteBackground();
        baseInflateFragment(inflater, R.layout.fragment_base_list);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
        recyclerView = (RecyclerView) findViewById(R.id.base_list);
        viewsReady();
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TopicsAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(item -> {
            if (item.isAnnounce()) return;
            if (item.isForum()) {
                IntentHandler.handle("http://4pda.ru/forum/index.php?showforum=" + item.getId());
                return;
            }
            Bundle args = new Bundle();
            args.putString(TabFragment.ARG_TITLE, item.getTitle());
            IntentHandler.handle("http://4pda.ru/forum/index.php?showtopic=" + item.getId() + "&view=getnewpost", args);
        });
        adapter.setOnLongItemClickListener(item -> {
            if (item.isAnnounce()) return;
            if (topicsDialogMenu == null) {
                topicsDialogMenu = new AlertDialogMenu<>();
                topicsDialogMenu.addItem("Скопировать ссылку", (context, data1) -> Utils.copyToClipBoard("http://4pda.ru/forum/index.php?showtopic=".concat(Integer.toString(data1.getId()))));
                topicsDialogMenu.addItem("Открыть форум темы", (context, data1) -> IntentHandler.handle("http://4pda.ru/forum/index.php?showforum=" + data.getId()));
                topicsDialogMenu.addItem("Добавить в избранное", ((context, data1) -> {
                    new AlertDialog.Builder(context.getContext())
                            .setItems(Favorites.SUB_NAMES, (dialog1, which1) -> {
                                FavoritesHelper.add(aBoolean -> {
                                    Toast.makeText(getContext(), aBoolean ? "Тема добавлена в избранное" : "Ошибочка вышла", Toast.LENGTH_SHORT).show();
                                }, data1.getId(), Favorites.SUB_TYPES[which1]);
                            })
                            .show();
                }));
            }

            new AlertDialog.Builder(getContext())
                    .setItems(topicsDialogMenu.getTitles(), (dialog, which) -> topicsDialogMenu.onClick(which, TopicsFragment.this, item))
                    .show();
        });

        paginationHelper.inflatePagination(getContext(), inflater, toolbar);
        paginationHelper.setupToolbar(toolbarLayout);
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
        refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Topics().getTopics(id, currentSt), this::onLoadThemes, new TopicsData(), v -> loadData());
    }

    private void onLoadThemes(TopicsData data) {
        refreshLayout.setRefreshing(false);
        recyclerView.scrollToPosition(0);
        this.data = data;

        setTitle(data.getTitle());
        adapter.clear();
        if (data.getForumItems().size() > 0)
            adapter.addItems(new Pair<>("Разделы", data.getForumItems()));
        if (data.getAnnounceItems().size() > 0)
            adapter.addItems(new Pair<>("Объявления", data.getAnnounceItems()));
        if (data.getPinnedItems().size() > 0)
            adapter.addItems(new Pair<>("Закрепленные темы", data.getPinnedItems()));
        adapter.addItems(new Pair<>("Темы", data.getTopicItems()));
        adapter.notifyDataSetChanged();
        paginationHelper.updatePagination(data.getPagination());
        setSubtitle(paginationHelper.getString());
    }
}
