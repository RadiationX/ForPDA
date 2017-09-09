package forpdateam.ru.forpda.fragments.devdb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.robohorse.pagerbullet.PagerBullet;

import java.util.ArrayList;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.devdb.models.Device;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.devdb.device.comments.CommentsFragment;
import forpdateam.ru.forpda.fragments.devdb.device.posts.PostsFragment;
import forpdateam.ru.forpda.fragments.devdb.device.specs.SpecsFragment;
import forpdateam.ru.forpda.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.imageviewer.ImageViewerActivity;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;
import forpdateam.ru.forpda.utils.rx.Subscriber;

/**
 * Created by radiationx on 08.08.17.
 */

public class DeviceFragment extends TabFragment {
    public final static String ARG_DEVICE_ID = "DEVICE_ID";
    private String deviceId = "";
    private Subscriber<Device> mainSubscriber = new Subscriber<>(this);
    private PagerBullet imagesPager;
    private TabLayout tabLayout;
    private TextView rating;
    private ViewPager fragmentsPager;
    private ProgressBar progressBar;
    private Device currentData;
    private RelativeLayout toolbarContent;
    private Observer statusBarSizeObserver = (observable1, o) -> {
        if (toolbarContent != null) {
            CollapsingToolbarLayout.LayoutParams params = (CollapsingToolbarLayout.LayoutParams) toolbarContent.getLayoutParams();
            params.topMargin = /*App.getToolBarHeight(toolbarLayout.getContext()) +*/ App.getStatusBarHeight();
            toolbarContent.setLayoutParams(params);
        }
    };

    public DeviceFragment() {
        configuration.setDefaultTitle(App.getInstance().getString(R.string.fragment_title_device));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceId = getArguments().getString(ARG_DEVICE_ID, deviceId);
        }

        if (getChildFragmentManager().getFragments() != null) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            for (Fragment fragment : getChildFragmentManager().getFragments()) {
                transaction.remove(fragment);
            }
            transaction.commit();
            getChildFragmentManager().executePendingTransactions();
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setCardsBackground();
        baseInflateFragment(inflater, R.layout.fragment_device);
        ViewStub viewStub = (ViewStub) findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.toolbar_device);
        toolbarContent = (RelativeLayout) viewStub.inflate();

        imagesPager = (PagerBullet) findViewById(R.id.images_pager);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        rating = (TextView) findViewById(R.id.item_rating);
        viewsReady();

        toolbarTitleView.setShadowLayer(App.px2, 0, 0, App.getColorFromAttr(getContext(), R.attr.colorPrimary));
        toolbarSubtitleView.setShadowLayer(App.px2, 0, 0, App.getColorFromAttr(getContext(), R.attr.colorPrimary));

        toolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        toolbarLayout.setCollapsedTitleTextColor(Color.TRANSPARENT);
        toolbarLayout.setTitleEnabled(false);
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbarLayout.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED); // list other flags here by |
        toolbarLayout.setLayoutParams(params);


        CollapsingToolbarLayout.LayoutParams newParams = (CollapsingToolbarLayout.LayoutParams) toolbar.getLayoutParams();
        newParams.setCollapseMode(CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN);
        newParams.bottomMargin = App.px48;
        toolbar.setLayoutParams(newParams);
        toolbar.requestLayout();


        tabLayout = new TabLayout(getContext());
        CollapsingToolbarLayout.LayoutParams tabparams = new CollapsingToolbarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        tabparams.setCollapseMode(CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN);
        tabLayout.setLayoutParams(tabparams);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        toolbarLayout.addView(tabLayout);


        fragmentsPager = (ViewPager) findViewById(R.id.view_pager);
        tabLayout.setupWithViewPager(fragmentsPager);

        imagesPager.setIndicatorTintColorScheme(App.getColorFromAttr(getContext(), R.attr.default_text_color), App.getColorFromAttr(getContext(), R.attr.second_text_color));

        App.getInstance().addStatusBarSizeObserver(statusBarSizeObserver);
        return view;
    }

    private MenuItem copyLinkMenuItem, shareMenuItem, noteMenuItem, toBrandMenuItem, toBrandsMenuItem;

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        copyLinkMenuItem = getMenu().add(R.string.copy_link)
                .setOnMenuItemClickListener(item -> {
                    Utils.copyToClipBoard("https://4pda.ru/devdb/" + currentData.getId());
                    return true;
                });

        shareMenuItem = getMenu().add(R.string.share)
                .setOnMenuItemClickListener(item -> {
                    Utils.shareText("https://4pda.ru/devdb/" + currentData.getId());
                    return true;
                });

        noteMenuItem = getMenu().add(R.string.create_note)
                .setOnMenuItemClickListener(item -> {
                    String title = "DevDb: " + currentData.getBrandTitle() + " " + currentData.getTitle();
                    String url = "https://4pda.ru/devdb/" + currentData.getId();
                    NotesAddPopup.showAddNoteDialog(getContext(), title, url);
                    return true;
                });

        toBrandMenuItem = getMenu().add(R.string.devices)
                .setOnMenuItemClickListener(item -> {
                    IntentHandler.handle("https://4pda.ru/devdb/" + currentData.getCatId() + "/" + currentData.getBrandId());
                    return true;
                });

        toBrandsMenuItem = getMenu().add(R.string.devices)
                .setOnMenuItemClickListener(item -> {
                    IntentHandler.handle("https://4pda.ru/devdb/" + currentData.getCatId());
                    return true;
                });

        refreshToolbarMenuItems(false);
    }

    @Override
    protected void refreshToolbarMenuItems(boolean enable) {
        super.refreshToolbarMenuItems(enable);
        if (enable) {
            copyLinkMenuItem.setEnabled(true);
            shareMenuItem.setEnabled(true);
            noteMenuItem.setEnabled(true);
            toBrandMenuItem.setVisible(true);
            toBrandsMenuItem.setVisible(true);
        } else {
            copyLinkMenuItem.setEnabled(false);
            shareMenuItem.setEnabled(false);
            noteMenuItem.setEnabled(false);
            toBrandMenuItem.setVisible(false);
            toBrandsMenuItem.setVisible(false);
        }
    }

    @Override
    public void loadData() {
        super.loadData();
        refreshToolbarMenuItems(false);
        progressBar.setVisibility(View.VISIBLE);
        mainSubscriber.subscribe(RxApi.DevDb().getDevice(deviceId), this::onLoad, new Device());
    }

    private void onLoad(Device device) {
        currentData = device;
        progressBar.setVisibility(View.GONE);
        toBrandMenuItem.setTitle(currentData.getCatTitle() + " " + currentData.getBrandTitle());
        toBrandsMenuItem.setTitle(currentData.getCatTitle());
        refreshToolbarMenuItems(true);
        setTitle(currentData.getTitle());
        setSubtitle(currentData.getCatTitle() + " > " + currentData.getBrandTitle());


        ArrayList<String> urls = new ArrayList<>();
        ArrayList<String> fullUrls = new ArrayList<>();
        for (Pair<String, String> pair : currentData.getImages()) {
            urls.add(pair.first);
            fullUrls.add(pair.second);
        }
        ImagesAdapter imagesAdapter = new ImagesAdapter(getContext(), urls, fullUrls);
        imagesPager.setAdapter(imagesAdapter);

        FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getChildFragmentManager(), currentData);
        fragmentsPager.setAdapter(pagerAdapter);

        if (device.getRating() > 0) {
            rating.setText(Integer.toString(device.getRating()));
            rating.setBackground(App.getDrawableAttr(rating.getContext(), R.attr.count_background));
            rating.getBackground().setColorFilter(RxApi.DevDb().getColorFilter(device.getRating()));
            rating.setVisibility(View.VISIBLE);
            if (device.getComments().size() > 0) {
                rating.setClickable(true);
                rating.setOnClickListener(v -> fragmentsPager.setCurrentItem(1, true));
            }

        } else {
            rating.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getInstance().removeStatusBarSizeObserver(statusBarSizeObserver);
    }

    private class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
        private Device device;
        private ArrayList<Fragment> fragments = new ArrayList<>();
        private ArrayList<String> titles = new ArrayList<>();

        public FragmentPagerAdapter(FragmentManager fm, Device device) {
            super(fm);
            this.device = device;
            if (this.device.getSpecs().size() > 0) {
                fragments.add(new SpecsFragment().setDevice(this.device));
                titles.add(App.getInstance().getString(R.string.device_page_specs));
            }
            if (this.device.getComments().size() > 0) {
                fragments.add(new CommentsFragment().setDevice(this.device));
                titles.add(App.getInstance().getString(R.string.device_page_comments) + " (" + this.device.getComments().size() + ")");
            }
            if (this.device.getDiscussions().size() > 0) {
                fragments.add(new PostsFragment().setSource(PostsFragment.SRC_DISCUSSIONS).setDevice(this.device));
                titles.add(App.getInstance().getString(R.string.device_page_discussions) + " (" + this.device.getDiscussions().size() + ")");
            }
            if (this.device.getNews().size() > 0) {
                fragments.add(new PostsFragment().setSource(PostsFragment.SRC_NEWS).setDevice(this.device));
                titles.add(App.getInstance().getString(R.string.device_page_news) + " (" + this.device.getNews().size() + ")");
            }
            if (this.device.getFirmwares().size() > 0) {
                fragments.add(new PostsFragment().setSource(PostsFragment.SRC_FIRMWARES).setDevice(this.device));
                titles.add(App.getInstance().getString(R.string.device_page_firmwares) + " (" + this.device.getFirmwares().size() + ")");
            }
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


    public class ImagesAdapter extends PagerAdapter {
        //private SparseArray<View> views = new SparseArray<>();
        private LayoutInflater inflater;
        private ArrayList<String> urls;
        ArrayList<String> fullUrls;

        public ImagesAdapter(Context context, ArrayList<String> urls, ArrayList<String> fullUrls) {
            this.inflater = LayoutInflater.from(context);
            this.urls = urls;
            this.fullUrls = fullUrls;
        }


        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View imageLayout = inflater.inflate(R.layout.device_image_page, container, false);
            assert imageLayout != null;
            imageLayout.setOnClickListener(v -> ImageViewerActivity.startActivity(DeviceFragment.this.getContext(), fullUrls, position));
            container.addView(imageLayout, 0);
            loadImage(imageLayout, position);
            return imageLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        private void loadImage(View imageLayout, int position) {
            assert imageLayout != null;
            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image_view);
            ProgressBar progressBar = (ProgressBar) imageLayout.findViewById(R.id.progress_bar);
            ImageLoader.getInstance().displayImage(urls.get(position), imageView, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    progressBar.setVisibility(View.GONE);
                }
            });

        }
    }

}
