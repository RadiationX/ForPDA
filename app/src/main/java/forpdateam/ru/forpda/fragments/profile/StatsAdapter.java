package forpdateam.ru.forpda.fragments.profile;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.rxapi.apiclasses.ProfileRx;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 14.09.17.
 */

class StatsAdapter extends BaseAdapter<ProfileModel.Stat, StatsAdapter.StatHolder> {
    @Override
    public StatsAdapter.StatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StatHolder(inflateLayout(parent, R.layout.profile_sub_item_stat));
    }

    @Override
    public void onBindViewHolder(StatsAdapter.StatHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class StatHolder extends BaseViewHolder<ProfileModel.Stat> {
        private TextView title;
        private TextView value;

        StatHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_title);
            value = (TextView) itemView.findViewById(R.id.item_value);
            itemView.setOnClickListener(v -> IntentHandler.handle(getItem(getLayoutPosition()).getUrl()));
        }

        @Override
        public void bind(ProfileModel.Stat item) {
            title.setText(ProfileRx.getTypeString(item.getType()));
            value.setText(String.format(Locale.getDefault(), "%d", item.getValue()));
        }
    }
}
