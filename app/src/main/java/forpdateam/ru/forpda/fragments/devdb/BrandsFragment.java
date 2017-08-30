package forpdateam.ru.forpda.fragments.devdb;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Map;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.devdb.models.Brands;
import forpdateam.ru.forpda.fragments.ListFragment;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.devdb.adapters.BrandsAdapter;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.rx.Subscriber;

/**
 * Created by radiationx on 08.08.17.
 */

public class BrandsFragment extends ListFragment {
    public final static String ARG_CATEGORY_ID = "CATEGORY_ID";
    private final static String[] spinnerTitles = {"Телефоны", "Планшеты", "Эл. книги", "Смарт часы"};
    private final static String[] mansCats = {"phones", "pad", "ebook", "smartwatch"};
    private Subscriber<Brands> mainSubscriber = new Subscriber<>(this);
    private BrandsAdapter adapter;
    private int selected = 0;
    private Brands currentData;

    public BrandsFragment() {
        //configuration.setAlone(true);
        configuration.setDefaultTitle("Произовдители");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String categoryId = getArguments().getString(ARG_CATEGORY_ID);
            if (categoryId != null) {
                for (int i = 0; i < mansCats.length; i++) {
                    if (mansCats[i].equals(categoryId)) {
                        selected = i;
                        break;
                    }
                }

            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewsReady();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(this::loadData);
        titlesWrapper.setVisibility(View.GONE);
        toolbarSpinner.setVisibility(View.VISIBLE);

        adapter = new BrandsAdapter();
        recyclerView.setAdapter(adapter);


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinnerTitles);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toolbarSpinner.setAdapter(spinnerAdapter);
        toolbarSpinner.setPrompt("Category");
        toolbarSpinner.setSelection(selected);
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
            args.putString(BrandFragment.ARG_CATEGORY_ID, currentData.getCatId());
            args.putString(BrandFragment.ARG_BRAND_ID, item.getId());
            TabManager.getInstance().add(BrandFragment.class, args);
        });

        return view;
    }

    @Override
    public void loadData() {
        super.loadData();
        refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(RxApi.DevDb().getBrands(mansCats[selected]), this::onLoad, new Brands());
    }

    private void onLoad(Brands brands) {
        currentData = brands;
        refreshLayout.setRefreshing(false);
        adapter.clear();
        for (Map.Entry<String, ArrayList<Brands.Item>> entry : brands.getLetterMap().entrySet()) {
            adapter.addSection(new Pair<>(entry.getKey(), entry.getValue()));
        }
        adapter.notifyDataSetChanged();
        setTitle(brands.getCatTitle());
    }
}
