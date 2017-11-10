package forpdateam.ru.forpda.ui.fragments.news.details;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.news.models.DetailsPage;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.common.rx.Subscriber;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.ui.views.ScrimHelper;
import io.reactivex.Observable;

import static forpdateam.ru.forpda.common.Utils.log;

/**
 * Created by isanechek on 8/19/17.
 */

public class NewsDetailsFragment extends TabFragment {

    public static final String ARG_NEWS_URL = "ARG_NEWS_URL";
    public static final String ARG_NEWS_ID = "ARG_NEWS_ID";
    public static final String ARG_NEWS_COMMENT_ID = "ARG_NEWS_COMMENT_ID";
    public static final String ARG_NEWS_TITLE = "ARG_NEWS_TITLE";
    public static final String ARG_NEWS_AUTHOR_NICK = "ARG_NEWS_AUTHOR_NICK";
    public static final String ARG_NEWS_AUTHOR_ID = "ARG_NEWS_AUTHOR_ID";
    public static final String ARG_NEWS_COMMENTS_COUNT = "ARG_NEWS_COMMENTS_COUNT";
    public static final String ARG_NEWS_DATE = "ARG_NEWS_DATE";
    public static final String ARG_NEWS_IMAGE = "ARG_NEWS_IMAGE";

    public static final String OTHER_CASE = "news.to.details.other";

    private FrameLayout webViewContainer;
    private ViewPager fragmentsPager;
    private ProgressBar progressBar;
    private ProgressBar imageProgressBar;
    private ImageView detailsImage;

    private TextView detailsTitle;
    private TextView detailsNick;
    private TextView detailsCount;
    private TextView detailsDate;
    //private Realm realm;
    //private News news;
    private String newsUrl;
    private int newsId;
    private int commentId;
    private String newsTitle;
    private String newsNick;
    private int newsCount = -1;
    private String newsDate;
    private String newsImageUrl;
    private DetailsPage currentArticle;
    private Subscriber<DetailsPage> mainSubscriber = new Subscriber<>(this);

    public NewsDetailsFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_news));
        configuration.setUseCache(false); // back
        configuration.setAlone(false);
        configuration.setFitSystemWindow(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            newsUrl = getArguments().getString(ARG_NEWS_URL);
            newsId = getArguments().getInt(ARG_NEWS_ID, 0);
            commentId = getArguments().getInt(ARG_NEWS_COMMENT_ID, 0);
            newsTitle = getArguments().getString(ARG_NEWS_TITLE);
            newsNick = getArguments().getString(ARG_NEWS_AUTHOR_NICK);
            newsDate = getArguments().getString(ARG_NEWS_DATE);
            newsImageUrl = getArguments().getString(ARG_NEWS_IMAGE);
            newsCount = getArguments().getInt(ARG_NEWS_COMMENTS_COUNT, -1);

            Log.d("SUKA", "" + newsId + " : " + newsTitle + " : " + newsNick + " : " + newsDate + " : " + newsImageUrl);
            //realm = Realm.getDefaultInstance();
            //news = realm.where(News.class).equalTo("url", newsId).findFirst();


        } else log("Arguments null");
        if (getChildFragmentManager().getFragments() != null) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            for (Fragment fragment : getChildFragmentManager().getFragments()) {
                transaction.remove(fragment);
            }
            transaction.commit();
            getChildFragmentManager().executePendingTransactions();
        }
    }

    boolean scrim = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.fragment_article);
        ViewStub viewStub = (ViewStub) findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.toolbar_news_details);
        viewStub.inflate();
        fragmentsPager = (ViewPager) findViewById(R.id.view_pager);
        webViewContainer = (FrameLayout) findViewById(R.id.swipe_refresh_list);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        detailsImage = (ImageView) findViewById(R.id.article_image);
        detailsTitle = (TextView) findViewById(R.id.article_title);
        detailsNick = (TextView) findViewById(R.id.article_nick);
        detailsCount = (TextView) findViewById(R.id.article_comments_count);
        detailsDate = (TextView) findViewById(R.id.article_date);
        imageProgressBar = (ProgressBar) findViewById(R.id.article_progress_bar);

        detailsImage.setMaxHeight(App.px24 * 10);

        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbarLayout.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        toolbarLayout.setLayoutParams(params);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewsReady();

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
            setTabTitle(String.format(getString(R.string.fragment_tab_title_article), newsTitle));
            detailsTitle.setText(newsTitle);
        }
        if (newsNick != null) {
            detailsNick.setText(newsNick);
        }
        if (newsCount != -1) {
            detailsCount.setText(Integer.toString(newsCount));
        }
        if (newsDate != null) {
            detailsDate.setText(newsDate);
        }
        toolbarTitleView.setVisibility(View.GONE);
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        toolbar.getOverflowIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        getMenu().add(R.string.copy_link)
                .setOnMenuItemClickListener(menuItem -> {
                    Utils.copyToClipBoard("https://4pda.ru/index.php?p=" + newsId);
                    return false;
                });
        getMenu().add(R.string.share)
                .setOnMenuItemClickListener(menuItem -> {
                    Utils.shareText("https://4pda.ru/index.php?p=" + newsId);
                    return false;
                });
        getMenu().add(R.string.create_note)
                .setOnMenuItemClickListener(menuItem -> {
                    String title = newsTitle;
                    String url = "https://4pda.ru/index.php?p=" + newsId;
                    NotesAddPopup.showAddNoteDialog(getContext(), title, url);
                    return false;
                });
    }

    @Override
    public boolean onBackPressed() {
        if (fragmentsPager.getCurrentItem() == 1) {
            fragmentsPager.setCurrentItem(0);
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public boolean loadData() {
        if (!super.loadData()) {
            return false;
        }
        //webViewContainer.setRefreshing(true);
        progressBar.setVisibility(View.VISIBLE);
        loadCoverImage();
        Observable<DetailsPage> observable = null;
        if (newsUrl != null) {
            observable = RxApi.NewsList().getDetails(newsUrl);
        } else {
            observable = RxApi.NewsList().getDetails(newsId);
        }
        mainSubscriber.subscribe(observable, this::onLoadArticle, new DetailsPage(), v -> loadData());
        return true;
    }

    private void onLoadArticle(DetailsPage article) {
        currentArticle = article;
        article.setCommentId(commentId);
        progressBar.setVisibility(View.GONE);
        newsTitle = article.getTitle();
        newsNick = article.getAuthor();
        newsDate = article.getDate();
        newsId = article.getId();
        newsCount = article.getCommentsCount();
        if (newsImageUrl == null) {
            newsImageUrl = article.getImgUrl();
            loadCoverImage();
        }

        setTitle(newsTitle);
        setTabTitle(String.format(getString(R.string.fragment_tab_title_article), newsTitle));
        detailsTitle.setText(newsTitle);
        detailsNick.setText(newsNick);
        detailsDate.setText(newsDate);
        detailsCount.setText(Integer.toString(newsCount));


        FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getChildFragmentManager(), article);
        fragmentsPager.setAdapter(pagerAdapter);
        if (article.getCommentId() != 0) {
            appBarLayout.setExpanded(false, true);
            /*if (!isTalkBackEnabled()) {
                appBarLayout.setExpanded(false, true);
            }*/
            fragmentsPager.setCurrentItem(1, true);
        }

        detailsNick.setOnClickListener(v -> {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=" + currentArticle.getAuthorId());
        });

    }


    public ViewPager getFragmentsPager() {
        return fragmentsPager;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /*if (!disposable.isDisposed())
            disposable.dispose();*/
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

    private class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
        private DetailsPage article;
        private ArrayList<Fragment> fragments = new ArrayList<>();
        private ArrayList<String> titles = new ArrayList<>();

        public FragmentPagerAdapter(FragmentManager fm, DetailsPage article) {
            super(fm);
            this.article = article;

            fragments.add(new ArticleContentFragment().setArticle(this.article));
            titles.add(App.get().getString(R.string.news_page_content));

            fragments.add(new ArticleCommentsFragment().setArticle(this.article));
            titles.add(App.get().getString(R.string.news_page_comments));
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }


}
