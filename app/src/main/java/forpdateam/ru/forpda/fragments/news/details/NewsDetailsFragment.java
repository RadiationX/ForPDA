package forpdateam.ru.forpda.fragments.news.details;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.App;
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

    private FrameLayout webViewContainer;
    private ImageView detailsImage;
    private ExtendedWebView webView;
    private TextView detailsTitle;
    private TextView detailsNick;
    private TextView detailsDate;
    private Realm realm;
    private CompositeDisposable disposable;
    private NewsApi api;
    private News news;
    private String _id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configuration.setDefaultTitle("Новость");
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
        webViewContainer = (FrameLayout) findViewById(R.id.swipe_refresh_list);
        webView = getMainActivity().getWebViewsProvider().pull(getContext());
        webViewContainer.addView(webView);

        ViewStub viewStub = (ViewStub) findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.toolbar_news_details);
        viewStub.inflate();
        detailsImage = (ImageView) findViewById(R.id.news_details_image);
        detailsTitle = (TextView) findViewById(R.id.news_details_title);
        detailsNick = (TextView) findViewById(R.id.news_details_nick);
        detailsDate = (TextView) findViewById(R.id.news_details_date);
        viewsReady();
        //webViewContainer.setOnRefreshListener(this::loadData);
        //refreshLayoutStyle(webViewContainer);



        /*toolbar.removeViewAt(0);

        toolbarLayout.setTitleEnabled(true);
        toolbar.setTitle(news.title);
        toolbarLayout.setTitle(news.title);
        toolbarLayout.setExpandedTitleGravity(Gravity.BOTTOM | Gravity.LEFT);
        //toolbarLayout.setExpandedTitleMarginTop(App.px64 );
        //toolbarLayout.setScrimVisibleHeightTrigger(App.px36);
        toolbarLayout.setExpandedTitleColor(Color.RED);
        toolbarLayout.setCollapsedTitleTextColor(Color.BLUE);
        //toolbarLayout.setExpandedTitleTextAppearance(R.style.QText);
        toolbarLayout.setScrimAnimationDuration(225);*/
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbarLayout.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        toolbarLayout.setLayoutParams(params);
        toolbarTitleView.setVisibility(View.GONE);
        //toolbarLayout.requestLayout();
        setTitle(news.title);
        detailsTitle.setText(news.title);
        detailsNick.setText(news.author);
        detailsDate.setText(news.date);
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        toolbar.getOverflowIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void loadData() {
        super.loadData();
        //webViewContainer.setRefreshing(true);
        loadCoverImage();
        disposable.add(RxApi.NewsList().getDetails(_id)
                .filter(item -> item.getHtml() != null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(n -> {
                    insertData(n);
                    //webViewContainer.setRefreshing(false);
                    webView.loadDataWithBaseURL("https://4pda.ru/forum/", n.getHtml(), "text/html", "utf-8", null);
                }, throwable -> {
                    //webViewContainer.setRefreshing(false);
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
        ImageLoader.getInstance().displayImage(news.imgUrl, detailsImage);
    }


    private void insertData(NewsItem item) {
        realm.executeTransaction(r -> {
            r.insertOrUpdate(EntityMapping.mappingNews(news, item));
        });
    }

}
