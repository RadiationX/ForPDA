package forpdateam.ru.forpda.fragments.devdb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.ndevdb.models.Device;
import forpdateam.ru.forpda.api.ndevdb.models.Manufacturer;
import forpdateam.ru.forpda.api.others.pagination.Pagination;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.devdb.device.specs.SpecsFragment;
import forpdateam.ru.forpda.imageviewer.ImagesAdapter;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.pagination.PaginationHelper;

/**
 * Created by radiationx on 08.08.17.
 */

public class DeviceFragment extends TabFragment {
    private Subscriber<Device> mainSubscriber = new Subscriber<>(this);
    private ViewPager imagesPager;

    public DeviceFragment() {
        configuration.setDefaultTitle("Произовдитель");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setCardsBackground();
        baseInflateFragment(inflater, R.layout.fragment_device);
        ViewStub viewStub = (ViewStub) findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.toolbar_device);
        viewStub.inflate();

        imagesPager = (ViewPager) findViewById(R.id.images_pager);
        viewsReady();


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


        TabLayout tabLayout = new TabLayout(getContext());
        CollapsingToolbarLayout.LayoutParams tabparams = new CollapsingToolbarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        tabparams.setCollapseMode(CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN);
        tabLayout.setLayoutParams(tabparams);
        tabLayout.addTab(tabLayout.newTab().setText("Характеристики"));
        tabLayout.addTab(tabLayout.newTab().setText("Отзывы"));
        tabLayout.addTab(tabLayout.newTab().setText("Обсуждения"));
        tabLayout.addTab(tabLayout.newTab().setText("Публикации"));
        tabLayout.addTab(tabLayout.newTab().setText("Прошивки"));
        tabLayout.addTab(tabLayout.newTab().setText("Цены"));
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        toolbarLayout.addView(tabLayout);


        pager = (ViewPager) findViewById(R.id.view_pager);

        return view;
    }

    ViewPager pager;

    @Override
    public void loadData() {
        mainSubscriber.subscribe(RxApi.DevDb().getDevice("apple_iphone_5s"), this::onLoad, new Device());
    }

    private class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
        private Device device;

        public FragmentPagerAdapter(FragmentManager fm, Device device) {
            super(fm);
            this.device = device;
        }

        @Override
        public Fragment getItem(int position) {
            return new SpecsFragment().setDevice(device);
        }

        @Override
        public int getCount() {
            return 1;
        }
    }

    private void onLoad(Device device) {

        //title.setText(device.getTitle());
        setTitle(device.getTitle());
        ArrayList<String> urls = new ArrayList<>();
        for (Pair<String, String> pair : device.getImages()) {
            urls.add(pair.first);
        }
        ImagesAdapter adapter = new ImagesAdapter(getContext(), urls);

        imagesPager.setAdapter(adapter);

        FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getChildFragmentManager(), device);
        pager.setAdapter(pagerAdapter);
    }

    public class ImagesAdapter extends PagerAdapter {
        //private SparseArray<View> views = new SparseArray<>();
        private LayoutInflater inflater;
        private List<String> urls;

        public ImagesAdapter(Context context, List<String> urls) {
            this.inflater = LayoutInflater.from(context);
            this.urls = urls;
        }


        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.e("FORPDA_LOG", "instantiateItem " + position);
            View imageLayout = inflater.inflate(R.layout.device_image_page, container, false);
            assert imageLayout != null;
            container.addView(imageLayout, 0);
            loadImage(imageLayout, position);
            return imageLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.e("FORPDA_LOG", "destroyItem " + position);
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        private void loadImage(View imageLayout, int position) {
            assert imageLayout != null;
            ImageView photoView = (ImageView) imageLayout.findViewById(R.id.image_view);
            ImageLoader.getInstance().displayImage(urls.get(position), photoView);

        }
    }

}
