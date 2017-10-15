package forpdateam.ru.forpda.fragments.devdb;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Map;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.devdb.models.Brands;
import forpdateam.ru.forpda.fragments.RecyclerFragment;
import forpdateam.ru.forpda.fragments.devdb.adapters.BrandsAdapter;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.rx.Subscriber;

/**
 * Created by radiationx on 08.08.17.
 */

public class BrandsFragment extends RecyclerFragment implements BrandsAdapter.OnItemClickListener<Brands.Item> {
    public final static String ARG_CATEGORY_ID = "CATEGORY_ID";
    private final static String[] spinnerTitles = {
            App.get().getString(R.string.brands_category_phones),
            App.get().getString(R.string.brands_category_tabs),
            App.get().getString(R.string.brands_category_ebook),
            App.get().getString(R.string.brands_category_smartwatch)};
    private final static String[] mansCats = {"phones", "pad", "ebook", "smartwatch"};
    private Subscriber<Brands> mainSubscriber = new Subscriber<>(this);
    private BrandsAdapter adapter;
    private int selected = 0;
    private Brands currentData;

    public BrandsFragment() {
        //configuration.setAlone(true);
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_brands));
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewsReady();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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

        adapter.setOnItemClickListener(this);
    }

    @Override
    public boolean loadData() {
        if (!super.loadData()) {
            return false;
        }
        setRefreshing(true);
        mainSubscriber.subscribe(RxApi.DevDb().getBrands(mansCats[selected]), this::onLoad, new Brands());
        return true;
    }

    private void onLoad(Brands brands) {
        currentData = brands;
        setRefreshing(false);
        adapter.clear();
        for (Map.Entry<String, ArrayList<Brands.Item>> entry : brands.getLetterMap().entrySet()) {
            adapter.addSection(new Pair<>(entry.getKey(), entry.getValue()));
        }
        adapter.notifyDataSetChanged();
        setTitle(brands.getCatTitle());
    }

    @Override
    public void onItemClick(Brands.Item item) {
        Bundle args = new Bundle();
        args.putString(BrandFragment.ARG_CATEGORY_ID, currentData.getCatId());
        args.putString(BrandFragment.ARG_BRAND_ID, item.getId());
        TabManager.getInstance().add(BrandFragment.class, args);
    }

    @Override
    public boolean onItemLongClick(Brands.Item item) {
        return false;
    }
}
