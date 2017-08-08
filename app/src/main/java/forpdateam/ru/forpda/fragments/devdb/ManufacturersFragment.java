package forpdateam.ru.forpda.fragments.devdb;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.ndevdb.models.Manufacturers;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.devdb.adapters.ManufacturersAdapter;
import forpdateam.ru.forpda.fragments.favorites.FavoritesAdapter;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import io.reactivex.functions.Consumer;

/**
 * Created by radiationx on 08.08.17.
 */

public class ManufacturersFragment extends TabFragment {
    private final static String[] spinnerTitles = {"Телефоны", "Планшеты", "Эл. книги", "Смарт часы"};
    private final static String[] mansCats = {"phones", "pad", "ebook", "smartwatch"};
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private Subscriber<Manufacturers> mainSubscriber = new Subscriber<>(this);
    private ManufacturersAdapter adapter;
    private int selected = 0;
    private Manufacturers currentData;

    public ManufacturersFragment() {
        configuration.setAlone(true);
        configuration.setDefaultTitle("Произовдители");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setListsBackground();
        baseInflateFragment(inflater, R.layout.fragment_base_list);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
        recyclerView = (RecyclerView) findViewById(R.id.base_list);
        viewsReady();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(this::loadData);
        titlesWrapper.setVisibility(View.GONE);
        toolbarSpinner.setVisibility(View.VISIBLE);

        adapter = new ManufacturersAdapter();
        recyclerView.setAdapter(adapter);


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinnerTitles);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toolbarSpinner.setAdapter(spinnerAdapter);
        toolbarSpinner.setPrompt("Category");
        toolbarSpinner.setSelection(0);
        toolbarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected = position;
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        adapter.setOnItemClickListener(item -> {
            Bundle args = new Bundle();
            args.putString(ManufacturerFragment.ARG_CATEGORY_ID, currentData.getCatId());
            args.putString(ManufacturerFragment.ARG_MANUFACTURER_ID, item.getId());
            TabManager.getInstance().add(new Builder<>(ManufacturerFragment.class).setArgs(args).build());
        });

        return view;
    }

    @Override
    public void loadData() {
        refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(RxApi.DevDb().getManufacturers(mansCats[selected]), this::onLoad, new Manufacturers());
    }

    private void onLoad(Manufacturers manufacturers) {
        currentData = manufacturers;
        refreshLayout.setRefreshing(false);
        adapter.clear();
        for (Map.Entry<String, ArrayList<Manufacturers.Item>> entry : manufacturers.getLetterMap().entrySet()) {
            adapter.addSection(new Pair<>(entry.getKey(), entry.getValue()));
        }
        adapter.notifyDataSetChanged();
        setTitle(manufacturers.getCatTitle());
    }
}
