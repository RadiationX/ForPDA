package forpdateam.ru.forpda.fragments.devdb.device.specs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.ndevdb.models.Device;

/**
 * Created by radiationx on 08.08.17.
 */

public class SpecsFragment extends Fragment {
    private RecyclerView recyclerView;
    private Device device;

    public SpecsFragment() {
    }

    public Device getDevice() {
        return device;
    }

    public SpecsFragment setDevice(Device device) {
        this.device = device;
        return this;
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
        return view;
    }
}
