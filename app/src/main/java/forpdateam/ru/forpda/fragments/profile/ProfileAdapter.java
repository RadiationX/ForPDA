package forpdateam.ru.forpda.fragments.profile;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.fragments.devdb.BrandFragment;
import forpdateam.ru.forpda.utils.LinkMovementMethod;
import forpdateam.ru.forpda.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 14.09.17.
 */

public class ProfileAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private final static int STATS_VIEW_TYPE = 1;
    private final static int ABOUT_VIEW_TYPE = 2;
    private final static int INFO_VIEW_TYPE = 3;
    private final static int DEVICES_VIEW_TYPE = 4;
    private final static int CONTACTS_VIEW_TYPE = 5;
    private final static int NOTE_VIEW_TYPE = 6;
    private ArrayList<Integer> items = new ArrayList<>();
    private ProfileModel profileModel;
    private ClickListener clickListener;

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setProfile(ProfileModel profile) {
        items.add(STATS_VIEW_TYPE);
        if (profile.getAbout() != null) {
            items.add(ABOUT_VIEW_TYPE);
        }
        items.add(INFO_VIEW_TYPE);
        if (profile.getDevices().size() > 0) {
            items.add(DEVICES_VIEW_TYPE);
        }
        if (profile.getContacts().size() > 1) {
            items.add(CONTACTS_VIEW_TYPE);
        }
        if (profile.getNote() != null) {
            items.add(NOTE_VIEW_TYPE);
        }
        profileModel = profile;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case STATS_VIEW_TYPE:
                return new StatsHolder(inflateLayout(parent, R.layout.profile_item_stats));
            case ABOUT_VIEW_TYPE:
                return new AboutHolder(inflateLayout(parent, R.layout.profile_item_about));
            case INFO_VIEW_TYPE:
                return new InfoHolder(inflateLayout(parent, R.layout.profile_item_info));
            case DEVICES_VIEW_TYPE:
                return new DeviceHolder(inflateLayout(parent, R.layout.profile_item_devices));
            case CONTACTS_VIEW_TYPE:
                return new ContactHolder(inflateLayout(parent, R.layout.profile_item_contacts));
            case NOTE_VIEW_TYPE:
                return new NoteHolder(inflateLayout(parent, R.layout.profile_item_note));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case STATS_VIEW_TYPE:
                ((StatsHolder) holder).bind(profileModel);
                break;
            case ABOUT_VIEW_TYPE:
                ((AboutHolder) holder).bind(profileModel);
                break;
            case INFO_VIEW_TYPE:
                ((InfoHolder) holder).bind(profileModel);
                break;
            case DEVICES_VIEW_TYPE:
                ((DeviceHolder) holder).bind(profileModel);
                break;
            case CONTACTS_VIEW_TYPE:
                ((ContactHolder) holder).bind(profileModel);
                break;
            case NOTE_VIEW_TYPE:
                ((NoteHolder) holder).bind(profileModel);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    protected View inflateLayout(ViewGroup parent, @LayoutRes int id) {
        return LayoutInflater.from(parent.getContext()).inflate(id, parent, false);
    }

    private class StatsHolder extends BaseViewHolder<ProfileModel> {
        private RecyclerView list;
        private StatsAdapter adapter;

        StatsHolder(View itemView) {
            super(itemView);
            list = (RecyclerView) itemView.findViewById(R.id.profile_stats_list);
            list.setHasFixedSize(true);
            list.setLayoutManager(new LinearLayoutManager(list.getContext(), LinearLayoutManager.HORIZONTAL, false));
            adapter = new StatsAdapter();
            list.setAdapter(adapter);
            list.setNestedScrollingEnabled(false);
        }

        @Override
        public void bind(ProfileModel item) {
            if (adapter.getItemCount() == 0) {
                ArrayList<ProfileModel.Stat> list = new ArrayList<>(item.getStats());
                Collections.reverse(list);
                adapter.addAll(list);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private class AboutHolder extends BaseViewHolder<ProfileModel> {

        private TextView about;

        AboutHolder(View itemView) {
            super(itemView);
            about = (TextView) itemView.findViewById(R.id.profile_about_text);
        }

        @Override
        public void bind(ProfileModel item) {
            about.setText(item.getAbout());
            about.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private class InfoHolder extends BaseViewHolder<ProfileModel> {

        private RecyclerView list;
        private InfoAdapter adapter;

        InfoHolder(View itemView) {
            super(itemView);
            list = (RecyclerView) itemView.findViewById(R.id.profile_info_list);
            list.setHasFixedSize(true);
            list.setLayoutManager(new LinearLayoutManager(list.getContext()));
            list.setNestedScrollingEnabled(false);
            list.addItemDecoration(new BrandFragment.SpacingItemDecoration(App.px16, true));
            adapter = new InfoAdapter();
            list.setAdapter(adapter);
        }

        @Override
        public void bind(ProfileModel item) {
            if (adapter.getItemCount() == 0) {
                adapter.addAll(item.getInfo());
                adapter.notifyDataSetChanged();
            }
        }
    }

    private class DeviceHolder extends BaseViewHolder<ProfileModel> {
        private RecyclerView list;
        private DevicesAdapter adapter;

        DeviceHolder(View itemView) {
            super(itemView);
            list = (RecyclerView) itemView.findViewById(R.id.profile_devices_list);
            list.setHasFixedSize(true);
            list.setLayoutManager(new LinearLayoutManager(list.getContext()));
            list.setNestedScrollingEnabled(false);
            adapter = new DevicesAdapter();
            list.setAdapter(adapter);
        }

        @Override
        public void bind(ProfileModel item) {
            adapter.addAll(item.getDevices());
        }
    }

    private class ContactHolder extends BaseViewHolder<ProfileModel> {
        private RecyclerView list;
        private ContactsAdapter adapter;

        ContactHolder(View itemView) {
            super(itemView);
            list = (RecyclerView) itemView.findViewById(R.id.profile_contacts_list);
            list.setHasFixedSize(true);
            list.setLayoutManager(new LinearLayoutManager(list.getContext(), LinearLayoutManager.HORIZONTAL, false));
            adapter = new ContactsAdapter();
            list.setAdapter(adapter);
            list.setNestedScrollingEnabled(false);
        }

        @Override
        public void bind(ProfileModel item) {
            adapter.addAll(item.getContacts());
        }
    }

    private class NoteHolder extends BaseViewHolder<ProfileModel> {

        private EditText note;
        private Button save;

        NoteHolder(View itemView) {
            super(itemView);
            note = (EditText) itemView.findViewById(R.id.profile_note_text);
            save = (Button) itemView.findViewById(R.id.profile_save_note);
        }

        @Override
        public void bind(ProfileModel item) {
            note.setText(item.getNote());
        }
    }

    public interface ClickListener {
        void onSaveClick(String text);
    }
}
