package forpdateam.ru.forpda.fragments.devdb;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.ndevdb.models.Manufacturer;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.devdb.adapters.ManufacturerAdapter;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.messagepanel.AutoFitRecyclerView;

/**
 * Created by radiationx on 08.08.17.
 */

public class ManufacturerFragment extends TabFragment {
    public final static String ARG_CATEGORY_ID = "CATEGORY_ID";
    public final static String ARG_MANUFACTURER_ID = "MANUFACTURER_ID";
    private SwipeRefreshLayout refreshLayout;
    private AutoFitRecyclerView recyclerView;
    private Subscriber<Manufacturer> mainSubscriber = new Subscriber<>(this);
    private ManufacturerAdapter adapter;
    private String catId, manId;

    public ManufacturerFragment() {
        configuration.setDefaultTitle("Произовдитель");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            catId = getArguments().getString(ARG_CATEGORY_ID, null);
            manId = getArguments().getString(ARG_MANUFACTURER_ID, null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setCardsBackground();
        baseInflateFragment(inflater, R.layout.fragment_manufacturer);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
        recyclerView = (AutoFitRecyclerView) findViewById(R.id.base_list);
        viewsReady();
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(this::loadData);

        adapter = new ManufacturerAdapter();
        recyclerView.setColumnWidth(App.getInstance().dpToPx(144));
        recyclerView.setAdapter(adapter);
        try {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addItemDecoration(new SpacingItemDecoration(gridLayoutManager, App.getInstance().dpToPx(8)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        adapter.setOnItemClickListener(deviceItem -> {
            Bundle args = new Bundle();
            args.putString(DeviceFragment.ARG_DEVICE_ID, deviceItem.getId());
            TabManager.getInstance().add(new Builder<>(DeviceFragment.class).setArgs(args).build());
        });

        return view;
    }

    @Override
    public void loadData() {
        refreshLayout.setRefreshing(true);
        Log.d("MANFR", "START LOAD DATA");
        mainSubscriber.subscribe(RxApi.DevDb().getManufacturer(catId, manId), this::onLoad, new Manufacturer());
    }

    private void onLoad(Manufacturer manufacturer) {
        refreshLayout.setRefreshing(false);
        Log.d("MANFR", "END LOAD DATA");
        adapter.addAll(manufacturer.getDevices());
        setTitle(manufacturer.getTitle());
        setSubtitle(manufacturer.getCatTitle());
    }

    public static class SpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount = 1;
        private boolean fullWidth = false;
        private boolean includeEdge = true;
        private int spacing;
        private GridLayoutManager manager;

        public SpacingItemDecoration(GridLayoutManager manager, int spacing) {
            this.spacing = spacing;
            this.manager = manager;
        }

        public SpacingItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        public SpacingItemDecoration(int spacing, boolean fullWidth) {
            this.spacing = spacing;
            this.fullWidth = fullWidth;
        }


        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (manager != null) {
                spanCount = manager.getSpanCount();
            }

            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                if (!fullWidth) {
                    outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                    outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)
                }
                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                if (!fullWidth) {
                    outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                    outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                }
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
}
