package forpdateam.ru.forpda.views.pagination;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.others.pagination.Pagination;

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
    private TabLayout tabLayout;
    private Pagination pagination;
    private PaginationListener listener;

    public String getString() {
        return pagination == null || pagination.getAll() <= 1 ? null : Integer.toString(pagination.getCurrent()).concat("/").concat(Integer.toString(pagination.getAll()));
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public void inflatePagination(Context context, LayoutInflater inflater, View target) {
        this.context = context;
        tabLayout = (TabLayout) inflater.inflate(R.layout.toolbar_theme, (ViewGroup) target.getParent(), false);
        ((ViewGroup) target.getParent()).addView(tabLayout, ((ViewGroup) target.getParent()).indexOfChild(target));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chevron_double_left).setTag(TAG_FIRST));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chevron_left).setTag(TAG_PREV));
        tabLayout.addTab(tabLayout.newTab().setText("Выбор").setTag(TAG_SELECT));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chevron_right).setTag(TAG_NEXT));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chevron_double_right).setTag(TAG_LAST));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
        });
    }

    public void setupToolbar(CollapsingToolbarLayout collapsingToolbarLayout) {
        if (collapsingToolbarLayout == null) return;
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        collapsingToolbarLayout.setLayoutParams(params);
        collapsingToolbarLayout.setScrimVisibleHeightTrigger(App.px56 + App.px24);
    }

    private void selectPage(int pageNumber) {
        listener.onSelectedPage(pageNumber);
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

    public void selectPageDialog() {
        if (context == null)
            context = App.getContext();
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

    public interface PaginationListener {
        boolean onTabSelected(TabLayout.Tab tab);

        void onSelectedPage(int pageNumber);
    }
}
