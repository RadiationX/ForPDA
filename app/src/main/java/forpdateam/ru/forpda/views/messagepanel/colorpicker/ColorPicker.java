package forpdateam.ru.forpda.views.messagepanel.colorpicker;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.nostra13.universalimageloader.utils.L;
import com.thebluealliance.spectrum.SpectrumPalette;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.views.messagepanel.advanced.AdvancedPopup;
import forpdateam.ru.forpda.views.messagepanel.advanced.BasePanelItem;
import forpdateam.ru.forpda.views.messagepanel.advanced.CodesPanelItem;
import forpdateam.ru.forpda.views.messagepanel.advanced.SmilesPanelItem;

/**
 * Created by radiationx on 27.05.17.
 */

public class ColorPicker {
    private String[] titles = new String[]{"Material", "Forum"};

    public ColorPicker(Context context, SpectrumPalette.OnColorSelectedListener listener) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layoutContainer = (LinearLayout) inflater.inflate(R.layout.color_picker_layout, null);


        ViewPager viewPager = (ViewPager) layoutContainer.findViewById(R.id.color_picker_pager);

        List<ScrollView> viewList = new ArrayList<>();
        ScrollView scrollView = new ScrollView(context);
        ScrollView scrollView1 = new ScrollView(context);
        SpectrumPalette materialColors = new SpectrumPalette(context);
        materialColors.setColors(context.getResources().getIntArray(R.array.demo_colors));
        SpectrumPalette forumColors = new SpectrumPalette(context);
        forumColors.setColors(context.getResources().getIntArray(R.array.forum_colors));
        scrollView.addView(materialColors);
        scrollView1.addView(forumColors);
        viewList.add(scrollView);
        viewList.add(scrollView1);

        viewPager.setAdapter(new MyPagerAdapter(viewList));
        ((TabLayout) layoutContainer.findViewById(R.id.color_picker_tab_layout)).setupWithViewPager(viewPager);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(layoutContainer)
                .show();
        SpectrumPalette.OnColorSelectedListener mainListener = i -> {
            if (listener != null) {
                listener.onColorSelected(i);
            }
            dialog.dismiss();
        };
        materialColors.setOnColorSelectedListener(mainListener);
        forumColors.setOnColorSelectedListener(mainListener);

    }


    private class MyPagerAdapter extends PagerAdapter {
        List<ScrollView> pages = null;

        MyPagerAdapter(List<ScrollView> pages) {
            this.pages = pages;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ViewGroup v = pages.get(position);
            container.addView(v, 0);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return pages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
