package forpdateam.ru.forpda.fragments.news.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.news.NewsApi;
import forpdateam.ru.forpda.api.news.models.NewsItem;
import forpdateam.ru.forpda.data.news.entity.News;
import forpdateam.ru.forpda.data.news.local.EntityMapping;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.views.ExtendedWebView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 8/19/17.
 */

public class NewsDetailsFragment extends TabFragment {

    public static final String NEWS_ID = "news.to.details.id";
    public static final String OTHER_CASE = "news.to.details.other";

    private SwipeRefreshLayout refreshLayout;
    private ExtendedWebView webView;
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
            api = new NewsApi();
            news = realm.where(News.class).equalTo("url", _id).findFirst();


        } else log("Arguments null");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.news_details_fragment_layout);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
        webView = getMainActivity().getWebViewsProvider().pull(getContext());
        refreshLayout.addView(webView);
        viewsReady();
        refreshLayout.setOnRefreshListener(this::loadData);
        refreshLayoutStyle(refreshLayout);

        setTitle(news.title);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void loadData() {
        super.loadData();
        refreshLayout.setRefreshing(true);
        loadCoverImage();
        disposable.add(RxApi.NewsList().getDetails(_id)
                .filter(item -> item.getHtml() != null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(n -> {
                    insertData(n);
                    refreshLayout.setRefreshing(false);
                    webView.loadDataWithBaseURL("https://4pda.ru/forum/", n.getHtml(), "text/html", "utf-8", null);
                }, throwable -> {
                    if (refreshLayout.isRefreshing()) refreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(), R.string.news_opps, Toast.LENGTH_SHORT).show();
                }));
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


    private void insertData(NewsItem item) {
        realm.executeTransaction(r -> {
            r.insertOrUpdate(EntityMapping.mappingNews(news, item));
        });
    }

}
