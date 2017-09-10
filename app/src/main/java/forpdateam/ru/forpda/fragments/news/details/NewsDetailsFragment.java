package forpdateam.ru.forpda.fragments.news.details;

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
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.Utils;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.ScrimHelper;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 8/19/17.
 */

public class NewsDetailsFragment extends TabFragment {

    public static final String ARG_NEWS_ID = "ARG_NEWS_ID";
    public static final String ARG_NEWS_COMMENT_ID = "ARG_NEWS_COMMENT_ID";
    public static final String ARG_NEWS_TITLE = "ARG_NEWS_TITLE";
    public static final String ARG_NEWS_AUTHOR_NICK = "ARG_NEWS_AUTHOR_NICK";
    public static final String ARG_NEWS_AUTHOR_ID = "ARG_NEWS_AUTHOR_ID";
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
    private TextView detailsDate;
    //private Realm realm;
    //private News news;
    private int newsId;
    private int commentId;
    private String newsTitle;
    private String newsNick;
    private String newsDate;
    private String newsImageUrl;
    private Subscriber<DetailsPage> mainSubscriber = new Subscriber<>(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configuration.setDefaultTitle(App.getInstance().getString(R.string.fragment_title_news));
        configuration.setUseCache(false); // back
        configuration.setAlone(false);
        if (getArguments() != null) {
            newsId = getArguments().getInt(ARG_NEWS_ID, 0);
            commentId = getArguments().getInt(ARG_NEWS_COMMENT_ID, 0);
            newsTitle = getArguments().getString(ARG_NEWS_TITLE);
            newsNick = getArguments().getString(ARG_NEWS_AUTHOR_NICK);
            newsDate = getArguments().getString(ARG_NEWS_DATE);
            newsImageUrl = getArguments().getString(ARG_NEWS_IMAGE);

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
        baseInflateFragment(inflater, R.layout.news_details_fragment_layout);
        fragmentsPager = (ViewPager) findViewById(R.id.view_pager);
        webViewContainer = (FrameLayout) findViewById(R.id.swipe_refresh_list);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);


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
    public void loadData() {
        super.loadData();
        //webViewContainer.setRefreshing(true);
        progressBar.setVisibility(View.VISIBLE);
        loadCoverImage();
        mainSubscriber.subscribe(RxApi.NewsList().getDetails(newsId), this::onLoadArticle, new DetailsPage(), v -> loadData());
    }

    private void onLoadArticle(DetailsPage article) {
        article.setCommentId(commentId);
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

        FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getChildFragmentManager(), article);
        fragmentsPager.setAdapter(pagerAdapter);
        if (article.getCommentId() != 0) {
            appBarLayout.setExpanded(false, true);
            /*if (!isTalkBackEnabled()) {
                appBarLayout.setExpanded(false, true);
            }*/
            fragmentsPager.setCurrentItem(1, true);
        }


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
            Log.e("SUKA", "CREATE FragmentPagerAdapter " + article);

            fragments.add(new ArticleContentFragment().setArticle(this.article));
            titles.add(App.getInstance().getString(R.string.news_page_content));

            fragments.add(new ArticleCommentsFragment().setArticle(this.article));
            titles.add(App.getInstance().getString(R.string.news_page_comments));
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
