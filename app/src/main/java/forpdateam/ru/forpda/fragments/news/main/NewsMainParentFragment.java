package forpdateam.ru.forpda.fragments.news.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.news.Constants;
import forpdateam.ru.forpda.api.news.NewsApi;
import forpdateam.ru.forpda.data.news.entity.News;
import forpdateam.ru.forpda.data.news.local.EntityMapping;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.devdb.BrandFragment;
import forpdateam.ru.forpda.fragments.news.details.NewsDetailsFragment;
import forpdateam.ru.forpda.fragments.news.main.timeline.NewsListAdapter;
import forpdateam.ru.forpda.rxapi.RxApi;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

/**
 * Created by isanechek on 8/8/17.
 */

public class NewsMainParentFragment extends TabFragment implements
        NewsListAdapter.ItemClickListener {
    private SwipeRefreshLayout refreshLayout;
    private NewsListAdapter adapter;
    private NewsApi mApi;
    private String category = Constants.NEWS_CATEGORY_ALL;
    private CompositeDisposable mDisposable;
    private Realm realm;

    public NewsMainParentFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configuration.setDefaultTitle(getString(R.string.default_news_title));
        configuration.setAlone(true);
        configuration.setUseCache(true);
        realm = Realm.getDefaultInstance();
        mDisposable = new CompositeDisposable();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setCardsBackground();
        baseInflateFragment(inflater, R.layout.news_list_fragment);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.news_list_progress);
        progressBar.setVisibility(View.GONE);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.news_list_refresh);
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setVisibility(View.VISIBLE);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.news_list);

        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new BrandFragment.SpacingItemDecoration(App.px8, true));

        adapter = new NewsListAdapter();
        adapter.setOnClickListener(this);
        recyclerView.setAdapter(adapter);
        mApi = new NewsApi();
        viewsReady();
        return view;
    }

    @Override
    public void loadData() {
        super.loadData();
        refreshLayout.setRefreshing(true);
        page = 0;
        loadDataNews(page);
    }

    @Override
    public void loadCacheData() {
        super.loadCacheData();
        List<News> list = realm.where(News.class).findAll();
        d("from realm " + list.size());
        if (list.size() > 0) adapter.insertData(list);
        else loadData();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDisposable.isDisposed()) {
            mDisposable.clear();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDisposable.isDisposed()) {
            mDisposable.clear();
        }

        if (realm != null) {
            realm.close();
        }
    }

    private void d(String msg) {
        Log.d("PARENT", msg);
    }

    @Override
    public void itemClick(View view, int position) {
        ViewCompat.setTransitionName(view, String.valueOf(position) + "_image");
        Bundle args = new Bundle();
        args.putString(NewsDetailsFragment.NEWS_ID, adapter.getItem(position).url);
        args.putBoolean(NewsDetailsFragment.OTHER_CASE, true);
        TabManager.getInstance().add(new TabFragment.Builder<>(NewsDetailsFragment.class).setArgs(args).build());
    }

    int page = 0;
    @Override
    public void loadMore() {
        page++;
        mDisposable.add(RxApi.NewsList().getNews(category, page)
                .map(newsItems -> EntityMapping.mappingNews(category, newsItems))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    refreshLayout.setRefreshing(false);
                    if (res.size() > 0) {
                        adapter.insertMore(res);
                        realm.executeTransaction(r -> {
                            //if (r.where(News.class).findAll().size() > 0) r.delete(News.class);
                            r.insertOrUpdate(res);
                        });
                    }
                }, throwable -> {
                    refreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(), R.string.news_opps, Toast.LENGTH_SHORT).show();
                }));
    }

    private void loadDataNews(int page) {
        mDisposable.add(RxApi.NewsList().getNews(category, page)
                .map(newsItems -> EntityMapping.mappingNews(category, newsItems))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    refreshLayout.setRefreshing(false);
                    if (res.size() > 0) {
                        adapter.insertData(res);
                        realm.executeTransaction(r -> {
                            //if (r.where(News.class).findAll().size() > 0) r.delete(News.class);
                            r.insertOrUpdate(res);
                        });
                    }
                }, throwable -> {
                    refreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(), R.string.news_opps, Toast.LENGTH_SHORT).show();
                }));
    }

    private void toast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
