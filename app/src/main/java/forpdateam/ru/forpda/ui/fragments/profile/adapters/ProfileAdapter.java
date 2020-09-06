package forpdateam.ru.forpda.ui.fragments.profile.adapters;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.common.LinkMovementMethod;
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel;
import forpdateam.ru.forpda.presentation.ILinkHandler;
import forpdateam.ru.forpda.presentation.ISystemLinkHandler;
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment;
import forpdateam.ru.forpda.ui.views.DividerItemDecoration;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;

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
    private final static int WARNING_VIEW_TYPE = 7;
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
        if (!profile.getDevices().isEmpty()) {
            items.add(DEVICES_VIEW_TYPE);
        }
        if (profile.getContacts().size() > 1) {
            items.add(CONTACTS_VIEW_TYPE);
        }
        if (profile.getNote() != null) {
            items.add(NOTE_VIEW_TYPE);
        }
        if (!profile.getWarnings().isEmpty()) {
            items.add(WARNING_VIEW_TYPE);
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
                return new InfosHolder(inflateLayout(parent, R.layout.profile_item_list));
            case DEVICES_VIEW_TYPE:
                return new DevicesHolder(inflateLayout(parent, R.layout.profile_item_list));
            case CONTACTS_VIEW_TYPE:
                return new ContactsHolder(inflateLayout(parent, R.layout.profile_item_list));
            case NOTE_VIEW_TYPE:
                return new NoteHolder(inflateLayout(parent, R.layout.profile_item_note));
            case WARNING_VIEW_TYPE:
                return new WarningsHolder(inflateLayout(parent, R.layout.profile_item_list));
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
                ((InfosHolder) holder).bind(profileModel);
                break;
            case DEVICES_VIEW_TYPE:
                ((DevicesHolder) holder).bind(profileModel);
                break;
            case CONTACTS_VIEW_TYPE:
                ((ContactsHolder) holder).bind(profileModel);
                break;
            case NOTE_VIEW_TYPE:
                ((NoteHolder) holder).bind(profileModel);
                break;
            case WARNING_VIEW_TYPE:
                ((WarningsHolder) holder).bind(profileModel);
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
            adapter = new StatsAdapter(item -> clickListener.onStatClick(item));
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
        private ILinkHandler linkHandler = App.get().Di().getLinkHandler();

        private TextView about;

        AboutHolder(View itemView) {
            super(itemView);
            about = (TextView) itemView.findViewById(R.id.profile_about_text);
        }

        @Override
        public void bind(ProfileModel item) {
            about.setText(item.getAbout());
            about.setMovementMethod(new LinkMovementMethod(url -> linkHandler.handle(url, null)));
        }
    }

    private class InfosHolder extends BaseViewHolder<ProfileModel> {
        private TextView title;
        private RecyclerView list;
        private InfoAdapter adapter;

        InfosHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.profile_sub_title);
            list = (RecyclerView) itemView.findViewById(R.id.profile_sub_list);
            list.setHasFixedSize(true);
            list.setLayoutManager(new LinearLayoutManager(list.getContext()));
            list.setNestedScrollingEnabled(false);
            list.addItemDecoration(new DevicesFragment.SpacingItemDecoration(App.px16, true));
            adapter = new InfoAdapter();
            list.setAdapter(adapter);
            title.setText(R.string.profile_title_information);
        }

        @Override
        public void bind(ProfileModel item) {
            if (adapter.getItemCount() == 0) {
                adapter.addAll(item.getInfo());
                adapter.notifyDataSetChanged();
            }
        }
    }

    private class DevicesHolder extends BaseViewHolder<ProfileModel> {
        private TextView title;
        private RecyclerView list;
        private DevicesAdapter adapter;

        DevicesHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.profile_sub_title);
            list = (RecyclerView) itemView.findViewById(R.id.profile_sub_list);
            list.setHasFixedSize(true);
            list.setLayoutManager(new LinearLayoutManager(list.getContext()));
            list.setNestedScrollingEnabled(false);
            adapter = new DevicesAdapter(item -> clickListener.onDeviceClick(item));
            list.setAdapter(adapter);
            title.setText(R.string.profile_title_devices);
        }

        @Override
        public void bind(ProfileModel item) {
            adapter.addAll(item.getDevices());
        }
    }

    private class ContactsHolder extends BaseViewHolder<ProfileModel> {
        private TextView title;
        private RecyclerView list;
        private ContactsAdapter adapter;

        ContactsHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.profile_sub_title);
            list = (RecyclerView) itemView.findViewById(R.id.profile_sub_list);
            list.setHasFixedSize(true);
            list.setLayoutManager(new LinearLayoutManager(list.getContext(), LinearLayoutManager.HORIZONTAL, false));
            adapter = new ContactsAdapter(item -> clickListener.onContactClick(item));
            list.setAdapter(adapter);
            list.setNestedScrollingEnabled(false);
            title.setText(R.string.profile_title_contacts);
        }

        @Override
        public void bind(ProfileModel item) {
            List<ProfileModel.Contact> contacts = item.getContacts();
            if (contacts.get(0).getType() == ProfileModel.ContactType.QMS) {
                contacts.remove(0);
            }
            adapter.addAll(contacts);
        }
    }

    private class WarningsHolder extends BaseViewHolder<ProfileModel> {
        private TextView title;
        private RecyclerView list;
        private WarningsAdapter adapter;

        WarningsHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.profile_sub_title);
            list = (RecyclerView) itemView.findViewById(R.id.profile_sub_list);
            list.setHasFixedSize(true);
            list.setLayoutManager(new LinearLayoutManager(list.getContext()));
            list.setNestedScrollingEnabled(false);
            list.addItemDecoration(new DividerItemDecoration(list.getContext()));
            adapter = new WarningsAdapter();
            list.setAdapter(adapter);
            title.setText(R.string.profile_title_warnings);
        }

        @Override
        public void bind(ProfileModel item) {
            adapter.addAll(item.getWarnings());
        }
    }

    private class NoteHolder extends BaseViewHolder<ProfileModel> {

        private EditText note;
        private Button save;

        NoteHolder(View itemView) {
            super(itemView);
            note = (EditText) itemView.findViewById(R.id.profile_note_text);
            save = (Button) itemView.findViewById(R.id.profile_save_note);
            save.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onSaveClick(note.getText().toString());
                }
            });
        }

        @Override
        public void bind(ProfileModel item) {
            note.setText(item.getNote());
        }
    }

    public interface ClickListener {
        void onSaveClick(String text);

        void onContactClick(ProfileModel.Contact item);

        void onDeviceClick(ProfileModel.Device item);

        void onStatClick(ProfileModel.Stat item);
    }
}
