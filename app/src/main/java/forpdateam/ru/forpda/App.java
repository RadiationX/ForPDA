package forpdateam.ru.forpda;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;

/**
 * Created by radiationx on 28.07.16.
 */
public class App extends android.app.Application {
    private static App INSTANCE = null;
    private SharedPreferences preferences;

    public App() {
        INSTANCE = this;
    }

    public static App getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new App();
        }
        return INSTANCE;
    }

    public static Context getContext() {
        return getInstance();
    }

    public static String getSystemDir() {
        File dir = App.getInstance().getFilesDir();
        if (dir == null)
            dir = App.getInstance().getExternalFilesDir(null);

        String res = dir.getPath();
        if (!res.endsWith(File.separator))
            res = res.concat(File.separator);
        return res;
    }

    public SharedPreferences getPreferences() {
        if (preferences == null)
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences;
    }
}
