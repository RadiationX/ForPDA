package forpdateam.ru.forpda.fragments.mentions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.mentions.models.MentionItem;
import forpdateam.ru.forpda.api.mentions.models.MentionsData;
import forpdateam.ru.forpda.fragments.ListFragment;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.pagination.PaginationHelper;

/**
 * Created by radiationx on 21.01.17.
 */

public class MentionsFragment extends ListFragment implements MentionsAdapter.OnItemClickListener<MentionItem> {
    private MentionsAdapter adapter;
    private Subscriber<MentionsData> mainSubscriber = new Subscriber<>(this);

    private PaginationHelper paginationHelper;
    private MentionsData data;
    private int currentSt = 0;


    public MentionsFragment() {
        configuration.setAlone(true);
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_mentions));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewsReady();
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
        //recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        adapter = new MentionsAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void loadData() {
        super.loadData();
        refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Mentions().getMentions(currentSt), this::onLoadThemes, new MentionsData(), v -> loadData());
    }

    private void onLoadThemes(MentionsData data) {
        refreshLayout.setRefreshing(false);

        this.data = data;
        adapter.addAll(data.getItems());
        paginationHelper.updatePagination(data.getPagination());
        setSubtitle(paginationHelper.getTitle());
        listScrollTop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        paginationHelper.destroy();
    }

    @Override
    public void onItemClick(MentionItem item) {
        Bundle args = new Bundle();
        args.putString(TabFragment.ARG_TITLE, item.getTitle());
        IntentHandler.handle(item.getLink(), args);
    }

    @Override
    public boolean onItemLongClick(MentionItem item) {
        return false;
    }
}
