package forpdateam.ru.forpda.fragments.news.details;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.views.ExtendedWebView;
import forpdateam.ru.forpda.views.ScrimHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 8/19/17.
 */

public class NewsDetailsFragment extends TabFragment {

    public static final String ARG_NEWS_ID = "ARG_NEWS_ID";
    public static final String ARG_NEWS_TITLE = "ARG_NEWS_TITLE";
    public static final String ARG_NEWS_AUTHOR_NICK = "ARG_NEWS_AUTHOR_NICK";
    public static final String ARG_NEWS_AUTHOR_ID = "ARG_NEWS_AUTHOR_ID";
    public static final String ARG_NEWS_DATE = "ARG_NEWS_DATE";
    public static final String ARG_NEWS_IMAGE = "ARG_NEWS_IMAGE";

    public static final String OTHER_CASE = "news.to.details.other";

    private FrameLayout webViewContainer;
    private ImageView detailsImage;
    private ExtendedWebView webView;
    private TextView detailsTitle;
    private TextView detailsNick;
    private TextView detailsDate;
    //private Realm realm;
    private CompositeDisposable disposable;
    //private News news;
    private int newsId;
    private String newsTitle;
    private String newsNick;
    private String newsDate;
    private String newsImageUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configuration.setDefaultTitle("Новость");
        configuration.setUseCache(false); // back
        configuration.setAlone(false);
        disposable = new CompositeDisposable();
        if (getArguments() != null) {
            newsId = getArguments().getInt(ARG_NEWS_ID);
            newsTitle = getArguments().getString(ARG_NEWS_TITLE);
            newsNick = getArguments().getString(ARG_NEWS_AUTHOR_NICK);
            newsDate = getArguments().getString(ARG_NEWS_DATE);
            newsImageUrl = getArguments().getString(ARG_NEWS_IMAGE);
            Log.d("SUKA", "" + newsId + " : " + newsTitle + " : " + newsNick + " : " + newsDate + " : " + newsImageUrl);
            //realm = Realm.getDefaultInstance();
            //news = realm.where(News.class).equalTo("url", newsId).findFirst();


        } else log("Arguments null");
    }

    boolean scrim = false;

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
        toolbar.setTitle(news.newsTitle);
        toolbarLayout.setTitle(news.newsTitle);
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

        ScrimHelper scrimHelper = new ScrimHelper(appBarLayout, toolbarLayout);
        scrimHelper.setScrimListener(scrim1 -> {
            if (scrim1) {
                toolbar.getNavigationIcon().clearColorFilter();
                toolbar.getOverflowIcon().clearColorFilter();
                toolbarTitleView.setVisibility(View.VISIBLE);
            } else {
                toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                toolbar.getOverflowIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                toolbarTitleView.setVisibility(View.GONE);
            }
        });

        //toolbarLayout.requestLayout();
        setTitle(newsTitle);
        detailsTitle.setText(newsTitle);
        detailsNick.setText(newsNick);
        detailsDate.setText(newsDate);
        toolbarTitleView.setVisibility(View.GONE);
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
        disposable.add(RxApi.NewsList().getDetails(newsId)
                .filter(item -> item.getHtml() != null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(article -> {
                    newsTitle = article.getTitle();
                    newsNick = article.getAuthor();
                    newsDate = article.getDate();
                    newsImageUrl = article.getImgUrl();
                    setTitle(newsTitle);
                    detailsTitle.setText(newsTitle);
                    detailsNick.setText(newsNick);
                    detailsDate.setText(newsDate);
                    loadCoverImage();
                    webView.loadDataWithBaseURL("https://4pda.ru/forum/", article.getHtml(), "text/html", "utf-8", null);
                }, throwable -> {
                    Toast.makeText(getActivity(), R.string.news_opps, Toast.LENGTH_SHORT).show();
                }));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable.isDisposed()) disposable.dispose();
        //if (realm != null) realm.close();
    }

    private void loadCoverImage() {
        ImageLoader.getInstance().displayImage(newsImageUrl, detailsImage);
    }


    /*private void insertData(DetailsPage item) {
        realm.executeTransaction(r -> {
            r.insertOrUpdate(EntityMapping.mappingNews(news, item));
        });
    }*/

}
