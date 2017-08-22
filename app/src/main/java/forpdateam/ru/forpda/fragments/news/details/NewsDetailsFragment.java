package forpdateam.ru.forpda.fragments.news.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.news.NewsApi;
import forpdateam.ru.forpda.api.news.models.NewsItem;
import forpdateam.ru.forpda.data.news.entity.News;
import forpdateam.ru.forpda.data.news.local.EntityMapping;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.news.details.blocks.InfoBlock;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 8/19/17.
 */

public class NewsDetailsFragment extends TabFragment {

    public static final String NEWS_ID = "news.to.details.id";

    private SwipeRefreshLayout refresh;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private NewsDetailsAdapter adapter;
    private Realm realm;
    private CompositeDisposable disposable;
    private NewsApi api;
    private News news;
    private String _id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configuration.setDefaultTitle("");
        configuration.setUseCache(false); // back
        configuration.setAlone(false);
        disposable = new CompositeDisposable();
        if (getArguments() != null) {
            _id = getArguments().getString(NEWS_ID);
            realm = Realm.getDefaultInstance();
            news = realm.where(News.class).equalTo("url", _id).findFirst();
            api = new NewsApi();
        } else log("Arguments null");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.news_details_fragment_layout);
        refresh = (SwipeRefreshLayout) findViewById(R.id.news_details_content_refresh);
        refresh.setOnRefreshListener(this::loadData);
        refreshLayoutStyle(refresh);
        refresh.setEnabled(false);
        progressBar = (ProgressBar) findViewById(R.id.news_details_progress);
        recyclerView = (RecyclerView) findViewById(R.id.news_details_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(false);
        adapter = new NewsDetailsAdapter();
        recyclerView.setAdapter(adapter);
        viewsReady();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(news.title);
        if (toolbarBackground.getVisibility() == View.GONE) {
            toolbarBackground.setVisibility(View.VISIBLE);
            loadCoverImage();
        } else loadCoverImage();

    }

    @Override
    public void loadData() {
        super.loadData();
        adapter.insertData(new InfoBlock(news.title, news.author, news.date));
        loadFromNetwork();
    }

    @Override
    public void loadCacheData() {
        super.loadCacheData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable.isDisposed()) disposable.dispose();
        if (realm != null) realm.close();
    }

    private void loadCoverImage() {
        ImageLoader.getInstance().displayImage(news.imgUrl, toolbarBackground);
    }

    private void loadFromNetwork() {
        refresh.setRefreshing(true);
        disposable.add(api.loadTestDetails(_id)
                .filter(item -> item.getHtml() != null)
                .map((Function<NewsItem, List>) item -> api.action(item.getHtml()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(n -> {
                    if (refresh.isRefreshing()) refresh.setRefreshing(false);
                    adapter.insertData(n);

//                    if (news.body != null) {
//                        if (news.body.equals(n.getHtml())) {
//                            log("Yse puchkom, bro.))");
//                        } else {
////                            insertData(n);
//                            loadHtml(n.html);
//                        }
//                    } else {
////                        insertData(n);
//                        loadHtml(n.html);
//                    }

                }, throwable -> {
                    if (refresh.isRefreshing()) refresh.setRefreshing(false);
                    Toast.makeText(getActivity(), R.string.news_opps, Toast.LENGTH_SHORT).show();
                }));
    }

    private void insertData(NewsItem item) {
        realm.executeTransaction(r -> {
            r.insertOrUpdate(EntityMapping.mappingNews(news, item));
        });
    }
}
