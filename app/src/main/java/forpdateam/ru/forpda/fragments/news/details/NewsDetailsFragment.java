package forpdateam.ru.forpda.fragments.news.details;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.news.models.DetailsPage;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.qms.chat.QmsChatFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.IntentHandler;
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
    private ProgressBar progressBar;
    private ProgressBar imageProgressBar;
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
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        webView = getMainActivity().getWebViewsProvider().pull(getContext());
        webViewContainer.addView(webView, 0);
        //webView.addJavascriptInterface(this, JS_INTERFACE);
        registerForContextMenu(webView);
        webView.setWebViewClient(new ArticleWebViewClient());

        ViewStub viewStub = (ViewStub) findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.toolbar_news_details);
        viewStub.inflate();
        detailsImage = (ImageView) findViewById(R.id.article_image);
        detailsTitle = (TextView) findViewById(R.id.article_title);
        detailsNick = (TextView) findViewById(R.id.article_nick);
        detailsDate = (TextView) findViewById(R.id.article_date);
        imageProgressBar = (ProgressBar) findViewById(R.id.article_progress_bar);
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
        if (newsTitle != null) {
            setTitle(newsTitle);
            detailsTitle.setText(newsTitle);
        }
        if (newsNick != null) {
            detailsNick.setText(newsNick);
        }
        if (newsDate != null) {
            detailsDate.setText(newsDate);
        }
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
        progressBar.setVisibility(View.VISIBLE);
        loadCoverImage();
        disposable.add(RxApi.NewsList().getDetails(newsId)
                .filter(item -> item.getHtml() != null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onLoadArticle, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(getActivity(), R.string.news_opps, Toast.LENGTH_SHORT).show();
                }));
    }

    private void onLoadArticle(DetailsPage article) {
        progressBar.setVisibility(View.GONE);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable.isDisposed()) disposable.dispose();
        //if (realm != null) realm.close();
    }

    private void loadCoverImage() {
        if (newsImageUrl != null) {
            ImageLoader.getInstance().displayImage(newsImageUrl, detailsImage, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    imageProgressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    imageProgressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private class ArticleWebViewClient extends WebViewClient {

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleUri(Uri.parse(url));
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return handleUri(request.getUrl());
        }

        private boolean handleUri(Uri uri) {
            IntentHandler.handle(uri.toString());
            return true;
        }
    }
    /*private void insertData(DetailsPage item) {
        realm.executeTransaction(r -> {
            r.insertOrUpdate(EntityMapping.mappingNews(news, item));
        });
    }*/

}
