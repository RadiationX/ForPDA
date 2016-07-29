package forpdateam.ru.forpda;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.File;

import forpdateam.ru.forpda.api.Api;

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

    @Override
    public void onCreate() {
        super.onCreate();
        Api.Init();
        initImageLoader(this);
    }

    private static DisplayImageOptions.Builder options = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .resetViewBeforeLoading(true)
            .cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .handler(new Handler())
            .displayer(new FadeInBitmapDisplayer(500, true, true, false));

    public static DisplayImageOptions.Builder getDefaultOptionsUIL() {
        return options;
    }

    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(5 * 1024 * 1024)) // 2 Mb
                .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .defaultDisplayImageOptions(options.build())
                .build();

        ImageLoader.getInstance().init(config);
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
