package forpdateam.ru.forpda.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.favorites.Sorting;
import forpdateam.ru.forpda.fragments.favorites.FavoritesAdapter;
import forpdateam.ru.forpda.settings.Preferences;
import forpdateam.ru.forpda.views.pagination.PaginationHelper;

/**
 * Created by radiationx on 14.08.17.
 */

public abstract class ListFragment extends TabFragment {
    protected SwipeRefreshLayout refreshLayout;
    protected RecyclerView recyclerView;
    protected LinearLayout listContainer;
    protected NestedScrollView listScrollView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setCardsBackground();
        baseInflateFragment(inflater, R.layout.fragment_base_list);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
        listContainer = (LinearLayout) findViewById(R.id.list_container);
        recyclerView = (RecyclerView) findViewById(R.id.base_list);
        listScrollView = (NestedScrollView) findViewById(R.id.list_scroll_view);

        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setBackgroundColor(App.getColorFromAttr(getContext(), R.attr.background_for_lists));
        recyclerView.setHasFixedSize(true);
        refreshLayoutStyle(refreshLayout);
        return view;
    }

    protected void listScrollTop() {
        new Handler().postDelayed(() -> {
            listScrollView.fullScroll(View.FOCUS_UP);
            //recyclerView.smoothScrollToPosition(0);
            //appBarLayout.setExpanded(true, true);
        }, 225);
    }
}
