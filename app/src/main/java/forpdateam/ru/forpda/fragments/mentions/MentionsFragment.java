package forpdateam.ru.forpda.fragments.mentions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.mentions.models.MentionsData;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.pagination.PaginationHelper;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.rx.Subscriber;

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
                args.putString(TabFragment.TITLE_ARG, favItem.getTitle());
                IntentHandler.handle(favItem.getLink(), args);
            };

    private Subscriber<MentionsData> mainSubscriber = new Subscriber<>(this);

    private PaginationHelper paginationHelper = new PaginationHelper();
    private MentionsData data;
    private int currentSt = 0;

    @Override
    public String getDefaultTitle() {
        return "Упоминания";
    }

    @Override
    public boolean isAlone() {
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        setWhiteBackground();
        baseInflateFragment(inflater, R.layout.fragment_qms_themes);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        recyclerView = (RecyclerView) findViewById(R.id.qms_list_themes);
        viewsReady();
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
        //recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        adapter = new MentionsAdapter();
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void loadData() {
        refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(Api.Mentions().getMentions(currentSt), this::onLoadThemes, new MentionsData(), v -> loadData());
    }

    private void onLoadThemes(MentionsData data) {
        refreshLayout.setRefreshing(false);
        this.data = data;
        adapter.addAll(data.getItems());
        paginationHelper.updatePagination(data.getPagination());
        setSubtitle(paginationHelper.getString());
    }
}
