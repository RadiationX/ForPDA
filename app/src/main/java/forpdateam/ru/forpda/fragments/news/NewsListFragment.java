package forpdateam.ru.forpda.fragments.news;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.Date;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.newslist.models.NewsItem;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.realm.RealmMapping;
import forpdateam.ru.forpda.utils.ErrorHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by radiationx on 31.07.16.
 */
public class NewsListFragment extends TabFragment implements NewsListAdapter.OnItemClickListener,
        NewsListAdapter.OnItemLongClickListener {
    private static final String LINk = "http://4pda.ru";

    private Date date;
    private TextView text;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private NewsListAdapter adapter;
    private LinearLayoutManager manager;
    private View listProgress;
    private Realm realm;
    private RealmResults<NewsModel> results;
    private boolean mIsLastPage = false;
    private boolean mIsLoading = false;
    private int mCurrentPage = 1;

    @Override
    public String getTabUrl() {
        return LINk;
    }

    @Override
    public boolean isAlone() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        log("onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        baseInflateFragment(inflater, R.layout.news_list_fragment);
//        text = (TextView) findViewById(R.id.textView2);
        listProgress = findViewById(R.id.news_list_progress);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.news_refresh_layout);
        refreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorAccent));
        recyclerView = (RecyclerView) findViewById(R.id.news_list);

        log("onCreateView");
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshLayout.setOnRefreshListener(this::loadData);

        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);

        adapter = new NewsListAdapter();
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        viewsReady();
        bindView();
        date = new Date();
        log("onViewCreated");
    }

    @Override
    public void loadData() {
        bindData(LINk);
        log("loadDate");
    }

    @Override
    public void onItemClick(int position, View view) {

    }

    @Override
    public void onItemLongClick(int position, View view) {

    }

    private void bindView() {
        log("bindView");
        results = realm.where(NewsModel.class).findAllAsync();
        results = results.sort("date", Sort.DESCENDING);
        if (results.size() == 0) {
            if (listProgress.getVisibility() == View.GONE) {
                listProgress.setVisibility(View.VISIBLE);
            }
        } else {
            if (listProgress.getVisibility() == View.VISIBLE) {
                listProgress.setVisibility(View.GONE);
            }
            refreshLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.addAll(results);
        }
    }

    private void bindData(String url) {
        log("bindData");
        Api.NewsList().getNews(url)
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, view1 -> loadData());
                    return new ArrayList<>();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::processAndAddData);
    }

    private void processAndAddData(ArrayList<NewsItem> items) {

        if (results.size() == 0) {
            insertData(items);
        } else {
            insertData(checkNewNews(items, results));
        }

        if (refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false);
        }
    }

    private void insertData(ArrayList<NewsItem> items) {
        Stream.of(items).map(RealmMapping::mappingNews)
                .forEach(news -> realm.executeTransactionAsync(r -> r.copyToRealmOrUpdate(news)));
    }

    private ArrayList<NewsItem> checkNewNews(ArrayList<NewsItem> list, RealmResults<NewsModel> results) {
        ArrayList<NewsItem> cache = new ArrayList<>();
        cache.clear();
        Stream.of(list)
                .filterNot(newNews -> Stream.of(results).anyMatch(oldNews -> newNews.getLink().equals(oldNews.getLink())))
                .forEach(cache::add);
        Toast.makeText(getContext(), cache.size() + " new news", Toast.LENGTH_SHORT).show();
        return cache;
    }

    private void log(String text) {
        Log.e("NewsModel", text);
    }
}
