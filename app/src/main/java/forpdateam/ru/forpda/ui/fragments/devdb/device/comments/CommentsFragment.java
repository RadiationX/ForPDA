package forpdateam.ru.forpda.ui.fragments.devdb.device.comments;

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
 * Created by radiationx on 09.08.17.
 */

public class CommentsFragment extends SubDeviceFragment {
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_fragment_specs, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.base_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        CommentsAdapter adapter = new CommentsAdapter();
        adapter.addAll(device.getComments());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new BrandFragment.SpacingItemDecoration(App.px8, true));
        return view;
    }
}
