package forpdateam.ru.forpda.fragments.topics;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.topcis.models.TopicsData;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.pagination.PaginationHelper;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.rx.Subscriber;

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

    @Override
    public String getDefaultTitle() {
        return "Темы форума";
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        initFabBehavior();
        setWhiteBackground();
        baseInflateFragment(inflater, R.layout.fragment_qms_themes);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        recyclerView = (RecyclerView) findViewById(R.id.qms_list_themes);
        viewsReady();
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TopicsAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(item -> {
            if (item.isAnnounce()) return;
            Bundle args = new Bundle();
            args.putString(TabFragment.TITLE_ARG, item.getTitle());
            IntentHandler.handle("http://4pda.ru/forum/index.php?showtopic=" + item.getId() + "&view=getnewpost", args);
        });

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
        return view;
    }


    @Override
    public void loadData() {
        refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(Api.Topics().getTopics(id, currentSt), this::onLoadThemes, new TopicsData(), v -> loadData());
    }

    private void onLoadThemes(TopicsData data) {
        refreshLayout.setRefreshing(false);
        this.data = data;

        setTitle(data.getTitle());
        adapter.clear();
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
