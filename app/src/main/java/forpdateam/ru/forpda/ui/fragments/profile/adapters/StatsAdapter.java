package forpdateam.ru.forpda.ui.fragments.profile.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel;
import forpdateam.ru.forpda.model.repository.temp.TempHelper;
import forpdateam.ru.forpda.presentation.ISystemLinkHandler;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 14.09.17.
 */

class StatsAdapter extends BaseAdapter<ProfileModel.Stat, StatsAdapter.StatHolder> {

    private StatHolder.Listener listener;

    public StatsAdapter(StatHolder.Listener listener) {
        this.listener = listener;
    }

    @Override
    public StatsAdapter.StatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StatHolder(inflateLayout(parent, R.layout.profile_sub_item_stat), listener);
    }

    @Override
    public void onBindViewHolder(StatsAdapter.StatHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class StatHolder extends BaseViewHolder<ProfileModel.Stat> {
        private TextView title;
        private TextView value;
        private ProfileModel.Stat currentItem;

        StatHolder(View itemView, Listener listener) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_title);
            value = (TextView) itemView.findViewById(R.id.item_value);
            itemView.setOnClickListener(v -> listener.onClick(currentItem));
        }

        @Override
        public void bind(ProfileModel.Stat item) {
            currentItem = item;
            title.setText(TempHelper.INSTANCE.getTypeString(item.getType()));
            value.setText(item.getValue());
        }

        interface Listener {
            void onClick(ProfileModel.Stat item);
        }
    }
}
