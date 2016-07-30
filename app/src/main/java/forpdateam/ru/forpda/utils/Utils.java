package forpdateam.ru.forpda.utils;

import android.os.Build;

/**
 * Created by isanechek on 30.07.16.
 */

public class Utils {
    public static boolean isMM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
