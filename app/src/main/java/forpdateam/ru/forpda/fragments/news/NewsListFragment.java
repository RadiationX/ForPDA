package forpdateam.ru.forpda.fragments.news;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import forpdateam.ru.forpda.Constants;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.data.Repository;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.news.models.NewsCallbackModel;
import forpdateam.ru.forpda.fragments.news.models.NewsModel;
import forpdateam.ru.forpda.fragments.news.presenter.NewsPresenter;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by radiationx on 31.07.16.
 */
public class NewsListFragment extends TabFragment implements INewsView, NewsListAdapter.OnItemClickListener,
        NewsListAdapter.OnItemLongClickListener, NewsListAdapter.OnLoadMoreCallback,
        NewsListAdapter.OnReloadDataListener {
    private static final String LINk = "http://4pda.ru";
    private static final String TAG = "NewsListFragment";

    private TextView text;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private NewsListAdapter adapter;
    private LinearLayoutManager manager;
    private View srProgress;
    private int pageSize = 1;
    private NewsPresenter presenter;


    @Override
    public String getTabUrl() {
        return LINk;
    }

    @Override
    public boolean isAlone() {
        return true;
    }

    @Override
    public boolean isUseCache() {
        return true;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        baseInflateFragment(inflater, R.layout.news_list_fragment);
//        text = (TextView) findViewById(R.id.textView2);
        log("onCreateView");
        srProgress = findViewById(R.id.news_list_progress);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.news_refresh_layout);
        refreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorAccent));
        recyclerView = (RecyclerView) findViewById(R.id.news_list);
        presenter = new NewsPresenter();
        presenter.bindView(this, Repository.getInstance());

        refreshLayout.setOnRefreshListener(() -> presenter.updateData(Constants.NEWS_CATEGORY_ALL, false));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        adapter = new NewsListAdapter();
        adapter.bindRecyclerView(recyclerView);
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
        adapter.setOnLoadMoreListener(this);
        adapter.setOnReloadDataListener(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        viewsReady();
    }

    @Override
    public void loadData() {
        log(TAG + " loadData");
        presenter.loadData(Constants.NEWS_CATEGORY_ALL);

    }

    @Override
    public void onItemClick(int position, View view) {

    }

    @Override
    public void onItemLongClick(int position, View view) {

    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.unbindView();
    }

    @Override
    public void onLoadMore() {
        pageSize++;
        log("Load More " + pageSize);
        adapter.addMoreLoadingProgress();
        presenter.loadMore(Constants.NEWS_CATEGORY_ALL, pageSize);
    }

    @Override
    public void showData(List<NewsModel> list) {
        refreshLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        log("show data -> " + list.size());
        adapter.addAll(list, false);
    }

    @Override
    public void showUpdateData(NewsCallbackModel model) {
        int size = model.getCache().size();
        if (size > 0) {
            Toast.makeText(getActivity(), size + " новых новостей", Toast.LENGTH_SHORT).show();
            adapter.addAll(model.getCache(), false);
            adapter.addMoreNewsLoadingItem(model.isShowMore());
        } else {
            Toast.makeText(getActivity(), "Нет новых новостей", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void showLoadMore(List<NewsModel> list, boolean hide) {
        if (!hide) {
            adapter.removeMoreLoadingProgress();
            adapter.addAll(list, true);
        } else {
            adapter.removeMoreLoadingProgress();
        }
    }

    @Override
    public void showMoreNewNews(NewsCallbackModel model) {

    }

    @Override
    public void showUpdateProgress(boolean show) {
        refreshLayout.setRefreshing(show);
    }

    @Override
    public void showBackgroundWorkProgress(boolean show) {
    }

    @Override
    public void showErrorView(Throwable throwable, @NonNull String codeError) {
    }


    @Override
    public void showPopUp(@NonNull String viewCode) {

    }

    @Override
    public void onReloadDataClick() {

    }
}
