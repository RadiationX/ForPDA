package forpdateam.ru.forpda.fragments.mentions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.mentions.models.MentionsData;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.pagination.PaginationHelper;

/**
 * Created by radiationx on 21.01.17.
 */

public class MentionsFragment extends TabFragment {
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private MentionsAdapter adapter;
    private MentionsAdapter.OnItemClickListener onItemClickListener =
            favItem -> {
                Bundle args = new Bundle();
                args.putString(TabFragment.ARG_TITLE, favItem.getTitle());
                IntentHandler.handle(favItem.getLink(), args);
            };

    private Subscriber<MentionsData> mainSubscriber = new Subscriber<>(this);

    private PaginationHelper paginationHelper = new PaginationHelper();
    private MentionsData data;
    private int currentSt = 0;


    public MentionsFragment(){
        configuration.setAlone(true);
        configuration.setDefaultTitle("Ответы");
    }

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
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
        //recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        adapter = new MentionsAdapter();
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void loadData() {
        refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Mentions().getMentions(currentSt), this::onLoadThemes, new MentionsData(), v -> loadData());
    }

    private void onLoadThemes(MentionsData data) {
        refreshLayout.setRefreshing(false);
        recyclerView.scrollToPosition(0);
        this.data = data;
        adapter.addAll(data.getItems());
        paginationHelper.updatePagination(data.getPagination());
        setSubtitle(paginationHelper.getString());

    }
}
