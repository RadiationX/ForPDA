package forpdateam.ru.forpda.ui.activities.imageviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import java.util.ArrayList;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.LocaleHelper;
import forpdateam.ru.forpda.common.Utils;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by radiationx on 24.05.17.
 */

public class ImageViewerActivity extends AppCompatActivity {
    private static final String IMAGE_URLS_KEY = "IMAGE_URLS_KEY";
    private static final String SELECTED_INDEX_KEY = "SELECTED_INDEX_KEY";
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private boolean mVisible;
    private PullBackLayout backLayout;
    private PullBackLayout.Callback pullBackCallback = new PullBackLayout.Callback() {
        @Override
        public void onPullStart() {
        }

        @Override
        public void onPull(@PullBackLayout.Direction int direction, float progress) {
            //
//        Animation animation = new AlphaAnimation(1, 0);
//        animation.setInterpolator(new AccelerateInterpolator());
//        animation.setStartOffset(1000);
//        animation.setDuration(1000);
//
//        AnimationSet set = new AnimationSet(false);
//        set.addAnimation(animation);
        }

        @Override
        public void onPullCancel(@PullBackLayout.Direction int direction) {
        }

        @Override
        public void onPullComplete(@PullBackLayout.Direction int direction) {
            finish();
        }
    };
    private HackyViewPager pager;
    private ImagesAdapter adapter;
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;

    public static void startActivity(Context context, String imageUrl) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        ArrayList<String> urls = new ArrayList<>();
        urls.add(imageUrl);
        intent.putExtra(IMAGE_URLS_KEY, urls);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, ArrayList<String> imageUrls, int selectedIndex) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(IMAGE_URLS_KEY, imageUrls);
        intent.putExtra(SELECTED_INDEX_KEY, selectedIndex);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.ImageViewTheme);
        setContentView(R.layout.activity_img_viewer);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        appBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        backLayout = (PullBackLayout) findViewById(R.id.image_viewer_pullBack);
        pager = (HackyViewPager) findViewById(R.id.img_viewer_pager);

        backLayout.setCallback(pullBackCallback);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        mVisible = true;

        int currentIndex = 0;
        ArrayList<String> extUrls = null;
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(IMAGE_URLS_KEY)) {
            extUrls = getIntent().getExtras().getStringArrayList(IMAGE_URLS_KEY);
        } else if (savedInstanceState != null && savedInstanceState.containsKey(IMAGE_URLS_KEY)) {
            extUrls = savedInstanceState.getStringArrayList(IMAGE_URLS_KEY);
        }

        if (extUrls != null) {
            urls.addAll(extUrls);
            extUrls.clear();
            for (String url : urls) {
                names.add(Utils.getFileNameFromUrl(url));
            }
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_INDEX_KEY)) {
            currentIndex = savedInstanceState.getInt(SELECTED_INDEX_KEY);
        } else if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(SELECTED_INDEX_KEY)) {
            currentIndex = getIntent().getExtras().getInt(SELECTED_INDEX_KEY);
        }


        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateTitle(position);
            }
        });
        adapter = new ImagesAdapter(this, urls);
        adapter.setTapListener((view, x, y) -> toggle());
        pager.setAdapter(adapter);
        pager.setCurrentItem(currentIndex);
        pager.setClipChildren(false);

        int finalCurrentIndex = currentIndex;
        toolbar.post(() -> updateTitle(finalCurrentIndex));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.save).setOnMenuItemClickListener(item -> {
            IntentHandler.handleDownload(urls.get(pager.getCurrentItem()));
            return false;
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(IMAGE_URLS_KEY, urls);
        outState.putInt(SELECTED_INDEX_KEY, pager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }


    @SuppressLint("DefaultLocale")
    private void updateTitle(int selectedPageIndex) {
        toolbar.setTitle(names.get(selectedPageIndex));
        toolbar.setSubtitle(String.format(getString(R.string.image_viewer_subtitle_Cur_All), selectedPageIndex + 1, urls.size()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
       /* if (adapter.views != null) {
            for (int i = 0; i < adapter.views.size(); i++) {
                if (adapter.views.get(i) == null) continue;
                ((ImageView) adapter.views.get(i).findViewById(R.id.photo_view)).setImageBitmap(null);
            }
        }*/
        System.gc();
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        mVisible = false;
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /*getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);*/
        getSupportActionBar().hide();
    }

    private void show() {
        mVisible = true;
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /*getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);*/
        getSupportActionBar().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        App.get().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
