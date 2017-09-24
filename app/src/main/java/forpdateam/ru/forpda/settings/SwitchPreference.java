package forpdateam.ru.forpda.settings;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.AttributeSet;

/**
 * Created by radiationx on 26.07.17.
 */

/*
* Исправляет самопроизвольные переключения настроек в киткате.
* Пи*дец, да.
* */
public class SwitchPreference extends SwitchPreferenceCompat {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitchPreference(Context context) {
        super(context);
    }
}
