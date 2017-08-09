package forpdateam.ru.forpda.fragments.devdb.device.posts;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.ndevdb.models.Device;
import forpdateam.ru.forpda.fragments.devdb.ManufacturerFragment;
import forpdateam.ru.forpda.fragments.devdb.device.SubDeviceFragment;
import forpdateam.ru.forpda.fragments.devdb.device.comments.CommentsAdapter;

/**
 * Created by radiationx on 09.08.17.
 */

public class PostsFragment extends SubDeviceFragment {
    public final static int SRC_DISCUSSIONS = 1;
    public final static int SRC_FIRMWARES = 2;
    public final static int SRC_NEWS = 3;

    private int source = 0;
    private RecyclerView recyclerView;

    public SubDeviceFragment setSource(int source) {
        this.source = source;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_fragment_specs, container, false);
        view.setBackgroundColor(App.getColorFromAttr(getContext(), R.attr.background_for_lists));
        recyclerView = (RecyclerView) view.findViewById(R.id.base_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        PostsAdapter adapter = new PostsAdapter();
        adapter.setSource(source);

        adapter.addAll(getList());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new ManufacturerFragment.SpacingItemDecoration(App.px8, true));
        return view;
    }

    private ArrayList<Device.PostItem> getList() {
        switch (source) {
            case SRC_DISCUSSIONS:
                return device.getDiscussions();
            case SRC_FIRMWARES:
                return device.getFirmwares();
            case SRC_NEWS:
                return device.getNews();
        }
        return null;
    }
}
