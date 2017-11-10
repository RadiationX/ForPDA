package forpdateam.ru.forpda.ui.fragments.settings;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by radiationx on 24.09.17.
 */

public class BaseSettingFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View rootView = getView();
        if (rootView != null) {
            RecyclerView list = (RecyclerView) rootView.findViewById(android.support.v7.preference.R.id.list);
            if (list != null) {
                list.setPadding(0, 0, 0, 0);
            }
        }
        setDividerHeight(0);
    }
}
