package forpdateam.ru.forpda.ui.fragments.devdb.device.specs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.ui.fragments.devdb.BrandFragment;
import forpdateam.ru.forpda.ui.fragments.devdb.device.SubDeviceFragment;

/**
 * Created by radiationx on 08.08.17.
 */

public class SpecsFragment extends SubDeviceFragment {
    private RecyclerView recyclerView;

    public SpecsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_fragment_specs, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.base_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        SpecsAdapter adapter = new SpecsAdapter();
        adapter.addAll(device.getSpecs());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new BrandFragment.SpacingItemDecoration(App.px8, true));
        return view;
    }
}
