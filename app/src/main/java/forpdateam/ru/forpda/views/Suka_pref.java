package forpdateam.ru.forpda.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import forpdateam.ru.forpda.R;

/**
 * Created by radiationx on 13.09.17.
 */

public class Suka_pref extends PreferenceCategory {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Suka_pref(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public Suka_pref(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Suka_pref(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Suka_pref(Context context) {
        super(context);
    }

    /*@Override
    protected View onCreateView(ViewGroup parent) {
        final LayoutInflater layoutInflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final ViewGroup layout = (ViewGroup) layoutInflater.inflate(R.layout.suka_pref, parent, false);

        return super.onCreateView(layout);
    }*/
}
