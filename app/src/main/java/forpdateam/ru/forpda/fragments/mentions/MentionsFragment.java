package forpdateam.ru.forpda.fragments.mentions;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.mentions.models.MentionsData;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.theme.adapters.ThemePagesAdapter;
import forpdateam.ru.forpda.utils.IntentHandler;

/**
 * Created by radiationx on 21.01.17.
 */

public class MentionsFragment extends TabFragment {
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private MentionsAdapter adapter;
    private MentionsAdapter.OnItemClickListener onItemClickListener =
            favItem -> {
                Bundle args = new Bundle();
                args.putString(TabFragment.TITLE_ARG, favItem.getTitle());
                IntentHandler.handle(favItem.getLink(), args);
            };

    private Subscriber<MentionsData> mainSubscriber = new Subscriber<>();

    protected TabLayout tabLayout;
    private MentionsData data;
    private int currentSt = 0;

    @Override
    public String getDefaultTitle() {
        return "Упоминания";
    }

    @Override
    public boolean isAlone() {
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        setWhiteBackground();
        baseInflateFragment(inflater, R.layout.fragment_qms_themes);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        recyclerView = (RecyclerView) findViewById(R.id.qms_list_themes);
        viewsReady();
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tabLayout = (TabLayout) inflater.inflate(R.layout.toolbar_theme, (ViewGroup) toolbar.getParent(), false);
        ((ViewGroup) toolbar.getParent()).addView(tabLayout, ((ViewGroup) toolbar.getParent()).indexOfChild(toolbar));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chevron_double_left).setTag("first"));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chevron_left).setTag("prev"));
        tabLayout.addTab(tabLayout.newTab().setText("Выбор").setTag("selectPage"));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chevron_right).setTag("next"));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chevron_double_right).setTag("last"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (refreshLayout.isRefreshing()) return;
                assert tab.getTag() != null;
                switch ((String) tab.getTag()) {
                    case "first":
                        firstPage();
                        break;
                    case "prev":
                        prevPage();
                        break;
                    case "selectPage":
                        selectPage(data);
                        break;
                    case "next":
                        nextPage();
                        break;
                    case "last":
                        lastPage();
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

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        collapsingToolbarLayout.setLayoutParams(params);
        collapsingToolbarLayout.setScrimVisibleHeightTrigger(App.px56 + App.px24);

        //recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));


        return view;
    }

    public void jumpToPage(int st) {
        currentSt = st;
        loadData();
    }


    public void firstPage() {
        if (data.getCurrentPage() <= 0) return;
        jumpToPage(0);
    }


    public void prevPage() {
        if (data.getCurrentPage() <= 1) return;
        jumpToPage((data.getCurrentPage() - 2) * data.getItemsPerPage());
    }


    public void nextPage() {
        if (data.getCurrentPage() == data.getAllPagesCount()) return;
        jumpToPage(data.getCurrentPage() * data.getItemsPerPage());
    }


    public void lastPage() {
        if (data.getCurrentPage() == data.getAllPagesCount()) return;
        jumpToPage((data.getAllPagesCount() - 1) * data.getItemsPerPage());
    }

    protected final ColorFilter colorFilter = new PorterDuffColorFilter(Color.argb(80, 255, 255, 255), PorterDuff.Mode.DST_IN);

    protected void updateNavigation() {
        if (data.getAllPagesCount() <= 1) {
            tabLayout.setVisibility(View.GONE);
            return;
        }
        tabLayout.setVisibility(View.VISIBLE);
        boolean prevDisabled = data.getCurrentPage() <= 1;
        boolean nextDisabled = data.getCurrentPage() == data.getAllPagesCount();
        TabLayout.Tab tab;
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            if (i == 2) continue;
            boolean b = i < 2 ? prevDisabled : nextDisabled;
            tab = tabLayout.getTabAt(i);
            if (tab != null && tab.getIcon() != null) {
                if (b)
                    tab.getIcon().setColorFilter(colorFilter);
                else
                    tab.getIcon().clearColorFilter();
            }
        }
    }

    private void selectPage(MentionsData pageData) {
        final int[] pages = new int[pageData.getAllPagesCount()];

        for (int i = 0; i < pageData.getAllPagesCount(); i++)
            pages[i] = i + 1;

        final ListView listView = new ListView(getContext());
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setFastScrollEnabled(true);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(new ThemePagesAdapter(getContext(), pages));
        listView.setItemChecked(pageData.getCurrentPage() - 1, true);
        listView.setSelection(pageData.getCurrentPage() - 1);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(listView)
                .show();

        if (dialog.getWindow() != null)
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        listView.setOnItemClickListener((adapterView, view1, i2, l) -> {
            if (listView.getTag() != null && !((Boolean) listView.getTag())) {
                return;
            }
            jumpToPage(i2 * pageData.getItemsPerPage());
            dialog.cancel();
        });
    }

    @Override
    public void loadData() {
        if (refreshLayout != null)
            refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(Api.Mentions().getMentions(currentSt), this::onLoadThemes, new MentionsData(), v -> loadData());
    }

    private void onLoadThemes(MentionsData data) {
        if (refreshLayout != null)
            refreshLayout.setRefreshing(false);
        this.data = data;
        adapter = new MentionsAdapter(data.getItems());
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);
        updateNavigation();
        setSubtitle("" + data.getCurrentPage() + "/" + data.getAllPagesCount());
    }
}
