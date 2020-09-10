package forpdateam.ru.forpda.ui.views.pagination;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;

import java.util.ArrayList;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination;
import forpdateam.ru.forpda.ui.DimensionHelper;
import forpdateam.ru.forpda.ui.DimensionsProvider;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by radiationx on 03.03.17.
 */

public class PaginationHelper {
    private final static int TAG_FIRST = 0;
    private final static int TAG_PREV = 1;
    private final static int TAG_SELECT = 2;
    private final static int TAG_NEXT = 3;
    private final static int TAG_LAST = 4;
    private final static ColorFilter colorFilter = new PorterDuffColorFilter(Color.argb(80, 255, 255, 255), PorterDuff.Mode.DST_IN);
    private Context context;
    private TabLayout tabLayoutInToolbar;

    private DimensionsProvider dimensionsProvider = App.get().Di().getDimensionsProvider();
    private CompositeDisposable disposables = new CompositeDisposable();

    private int currentPage = 0;

    private ArrayList<TabLayout> tabLayouts = new ArrayList<>();
    private Pagination pagination;
    private PaginationListener listener;
    private TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            if (listener.onTabSelected(tab) || tab.getTag() == null) return;
            switch ((Integer) tab.getTag()) {
                case TAG_FIRST:
                    firstPage();
                    break;
                case TAG_PREV:
                    prevPage();
                    break;
                case TAG_NEXT:
                    nextPage();
                    break;
                case TAG_LAST:
                    lastPage();
                    break;
                case TAG_SELECT:
                    selectPageDialog();
                    break;
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            onTabSelected(tab);
        }
    };


    public PaginationHelper(Activity context) {
        this.context = context;

    }

    private void updateDimens(DimensionHelper.Dimensions dimensions) {
        if (tabLayoutInToolbar != null) {
            CollapsingToolbarLayout.LayoutParams params = (CollapsingToolbarLayout.LayoutParams) tabLayoutInToolbar.getLayoutParams();
            params.topMargin = App.getToolBarHeight(tabLayoutInToolbar.getContext()) + dimensions.getStatusBar();
            tabLayoutInToolbar.setLayoutParams(params);
        }
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public void addInToolbar(LayoutInflater inflater, CollapsingToolbarLayout target, boolean enablePadding) {
        TabLayout tabLayout = (TabLayout) inflater.inflate(R.layout.pagination_toolbar, target, false);
        target.addView(tabLayout, target.indexOfChild(target.findViewById(R.id.toolbar)));
        tabLayoutInToolbar = tabLayout;
        if (enablePadding) {
            disposables.add(
                    dimensionsProvider
                            .observeDimensions()
                            .subscribe(dimensions -> {
                                if (tabLayoutInToolbar != null) {
                                    tabLayoutInToolbar.post(() -> {
                                        if (tabLayoutInToolbar != null) {
                                            updateDimens(dimensions);
                                        }
                                    });
                                }
                                updateDimens(dimensions);
                            })
            );
        }

        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) target.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        target.setLayoutParams(params);
        target.setScrimVisibleHeightTrigger(App.px56 + App.px24);
        setupTabLayout(tabLayout, true);
        tabLayouts.add(tabLayout);
        target.requestLayout();
    }

    public void addInList(LayoutInflater inflater, ViewGroup target) {
        TabLayout tabLayout = (TabLayout) inflater.inflate(R.layout.pagination_list, target, false);
        target.addView(tabLayout);
        setupTabLayout(tabLayout, false);
        tabLayouts.add(tabLayout);
        target.requestLayout();
    }

    private void setupTabLayout(TabLayout tabLayout, boolean firstLast) {
        if (firstLast) {
            tabLayout.addTab(tabLayout.newTab()
                    .setIcon(R.drawable.ic_toolbar_chevron_double_left)
                    .setTag(TAG_FIRST)
                    .setContentDescription(R.string.pagination_first));
        }

        tabLayout.addTab(tabLayout.newTab()
                .setIcon(R.drawable.ic_toolbar_chevron_left)
                .setTag(TAG_PREV)
                .setContentDescription(R.string.pagination_prev));

        tabLayout.addTab(tabLayout.newTab()
                .setText(R.string.pagination_select)
                .setTag(TAG_SELECT)
                .setContentDescription(R.string.pagination_select_desc));

        tabLayout.addTab(tabLayout.newTab()
                .setIcon(R.drawable.ic_toolbar_chevron_right)
                .setTag(TAG_NEXT)
                .setContentDescription(R.string.pagination_next));

        if (firstLast) {
            tabLayout.addTab(tabLayout.newTab()
                    .setIcon(R.drawable.ic_toolbar_chevron_double_right)
                    .setTag(TAG_LAST)
                    .setContentDescription(R.string.pagination_last));
        }

        tabLayout.addOnTabSelectedListener(tabSelectedListener);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    private void selectPage(int pageNumber) {
        currentPage = pageNumber;
        if (listener != null) {
            listener.onSelectedPage(pageNumber);
        }
    }

    public void firstPage() {
        if (pagination.getCurrent() <= 1) return;
        selectPage(pagination.isForum() ? 0 : 1);
    }

    public void prevPage() {
        if (pagination.getCurrent() <= 1) return;
        selectPage(pagination.getPage(pagination.getCurrent() - (pagination.isForum() ? 2 : 1)));
    }

    public void nextPage() {
        if (pagination.getCurrent() == pagination.getAll()) return;
        selectPage(pagination.getPage(pagination.getCurrent() + (pagination.isForum() ? 0 : 1)));
    }

    public void lastPage() {
        if (pagination.getCurrent() == pagination.getAll()) return;
        selectPage(pagination.getPage(pagination.getAll() - (pagination.isForum() ? 1 : 0)));
    }

    public void updatePagination(Pagination newPagination) {
        this.pagination = newPagination;
        for (TabLayout tabLayout : tabLayouts) {
            if (pagination.getAll() <= 1) {
                tabLayout.setVisibility(View.GONE);
                return;
            }
            tabLayout.setVisibility(View.VISIBLE);
            boolean prevDisabled = pagination.getCurrent() <= 1;
            boolean nextDisabled = pagination.getCurrent() == pagination.getAll();
            TabLayout.Tab tab;
            int tag;
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                tab = tabLayout.getTabAt(i);
                if (tab == null || tab.getTag() == null) return;
                tag = (Integer) tab.getTag();
                if ((tag) == TAG_SELECT) continue;
                if (tab.getIcon() != null) {
                    if ((tag == TAG_FIRST || tag == TAG_PREV) ? prevDisabled : nextDisabled)
                        tab.getIcon().setColorFilter(colorFilter);
                    else
                        tab.getIcon().clearColorFilter();
                }
            }
        }
    }

    public String getTitle() {
        return pagination == null || pagination.getAll() <= 1 ? null : Integer.toString(pagination.getCurrent()).concat("/").concat(Integer.toString(pagination.getAll()));
    }

    public void selectPageDialog() {
        final int[] pages = new int[pagination.getAll()];

        for (int i = 0; i < pagination.getAll(); i++)
            pages[i] = i + 1;

        final ListView listView = new ListView(context);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setFastScrollEnabled(true);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(new PaginationAdapter(context, pages));
        listView.setItemChecked(pagination.getCurrent() - 1, true);
        listView.setSelection(pagination.getCurrent() - 1);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(listView)
                .show();

        if (dialog.getWindow() != null)
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        listView.setOnItemClickListener((adapterView, view1, i2, l) -> {
            if (listView.getTag() != null && !((Boolean) listView.getTag())) {
                return;
            }
            selectPage(i2 * pagination.getPerPage());
            dialog.cancel();
        });
    }

    public void setListener(PaginationListener listener) {
        this.listener = listener;
    }

    public void destroy() {
        disposables.dispose();
    }

    public interface PaginationListener {
        boolean onTabSelected(TabLayout.Tab tab);

        void onSelectedPage(int pageNumber);
    }
}
