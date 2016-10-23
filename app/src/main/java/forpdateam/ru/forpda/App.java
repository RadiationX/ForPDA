package forpdateam.ru.forpda;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.content.res.AppCompatResources;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.rxbus.RxBus;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by radiationx on 28.07.16.
 */
public class App extends android.app.Application {
    private static App INSTANCE = new App();
    private SharedPreferences preferences;

    public static App getInstance() {
        return INSTANCE;
    }

    public App() {
        INSTANCE = this;
    }

    private RxBus rxBus;
    private MiniTemplator templator;

    public MiniTemplator getTemplator() {
        return templator;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        InputStream stream = null;
        try {
            stream = App.getInstance().getAssets().open("temp.html");
            templator = new MiniTemplator.Builder().build(stream, Charset.forName("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Api.Init();
        //init
        Client.getInstance();
        initImageLoader(this);
        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name("forpda.realm")
                .schemaVersion(1)
                .build();
        Realm.setDefaultConfiguration(configuration);
        rxBus = new RxBus();
        px2 = getContext().getResources().getDimensionPixelSize(R.dimen.dp2);
        px4 = getContext().getResources().getDimensionPixelSize(R.dimen.dp4);
        px6 = getContext().getResources().getDimensionPixelSize(R.dimen.dp6);
        px8 = getContext().getResources().getDimensionPixelSize(R.dimen.dp8);
        px12 = getContext().getResources().getDimensionPixelSize(R.dimen.dp12);
        px14 = getContext().getResources().getDimensionPixelSize(R.dimen.dp14);
        px16 = getContext().getResources().getDimensionPixelSize(R.dimen.dp16);
        px24 = getContext().getResources().getDimensionPixelSize(R.dimen.dp24);
        px32 = getContext().getResources().getDimensionPixelSize(R.dimen.dp32);
        px36 = getContext().getResources().getDimensionPixelSize(R.dimen.dp36);
        px40 = getContext().getResources().getDimensionPixelSize(R.dimen.dp40);
        px48 = getContext().getResources().getDimensionPixelSize(R.dimen.dp48);
        px56 = getContext().getResources().getDimensionPixelSize(R.dimen.dp56);
        px64 = getContext().getResources().getDimensionPixelSize(R.dimen.dp64);

        //Для более быстрого доступа к drawable при работе программы
        for (Field f : R.drawable.class.getFields()) {
            try {
                if (!f.getName().contains("abc_"))
                    drawableHashMap.put(f.getInt(f), AppCompatResources.getDrawable(App.getContext(), f.getInt(f)));
            } catch (Exception ignore) {
            }
        }
    }

    public static HashMap<Integer, Drawable> drawableHashMap = new HashMap<>();

    public static Drawable getAppDrawable(int id) {
        return drawableHashMap.get(id);
    }

    private static DisplayImageOptions.Builder options = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .resetViewBeforeLoading(true)
            .cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
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

    public SharedPreferences getPreferences() {
        if (preferences == null)
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences;
    }

    public RxBus bus() {
        return rxBus;
    }

    public static int px2, px4, px6, px8, px12, px14, px16, px24, px32, px36, px40, px48, px56, px64;
}
