package forpdateam.ru.forpda;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.content.res.AppCompatResources;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.data.Repository;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by radiationx on 28.07.16.
 */
public class App extends android.app.Application {
    private static App INSTANCE = new App();
    private SharedPreferences preferences;
    private static int savedKeyboardHeight = 0;
    public static int keyboardHeight = 0;
    public static int statusBarHeight = 0;

    public static int getStatusBarHeight() {
        return statusBarHeight;
    }

    public static void setStatusBarHeight(int statusBarHeight) {
        App.statusBarHeight = statusBarHeight;
    }

    public static int getKeyboardHeight() {
        return keyboardHeight;
    }

    public static void setKeyboardHeight(int newKeyboardHeight) {
        keyboardHeight = newKeyboardHeight;
        if (keyboardHeight == savedKeyboardHeight) return;
        App.getInstance().getPreferences().edit().putInt("keyboard_height", keyboardHeight).apply();
    }

    public static App getInstance() {
        return INSTANCE;
    }

    public App() {
        INSTANCE = this;
    }

    private MiniTemplator templator;

    public MiniTemplator getTemplator() {
        return templator;
    }


    /*private final static Pattern p = Pattern.compile("(?:[^\\s\\-—.,:;&?=#@><\\{\\}\\[\\]!~`*^%$\\|\"'\\/][\\s\\S][^\\s\\-—.,:;&?=#@><\\{\\}\\[\\]!~`*^%$\\|\"'\\/]*)");
    private Matcher matcher1;
    private Matcher matcher2;

    public boolean notStrictEquals(final String s1, final String s2) {
        matcher1 = matcher1 == null ? p.matcher(s1) : matcher1.reset(s1);
        matcher2 = matcher2 == null ? p.matcher(s2) : matcher2.reset(s2);
        while (matcher1.find() & matcher2.find())
            if (!matcher1.group().equalsIgnoreCase(matcher2.group())) return false;
        return true;
    }

    final String s1 = "Искусственная гравитация в Sci-Fi. Ищем истину";
    final String s2 = "Искусственная гравитация в Sci-Fi — ищем истину";*/

    @Override
    public void onCreate() {
        super.onCreate();
        /*long time = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            //Log.d("kek", "eq1 "+notStrictEquals(s1,s2));
            notStrictEquals(s1, s2);
        }
        Log.d("kek", "eq1 time: " + (System.currentTimeMillis() - time));
        *//*time = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            //Log.d("kek", "eq1 "+notStrictEquals(s1,s2));
            notStrictEquals2(s1, s2);
        }
        Log.d("kek", "eq2 time: " + (System.currentTimeMillis() - time));*//*
        //Log.d("kek", "eq2 "+notStrictEquals2(s1,s2));*/

        InputStream stream = null;
        try {
            stream = App.getInstance().getAssets().open("temp.html");
            try {
                templator = new MiniTemplator.Builder().build(stream, Charset.forName("utf-8"));
            } catch (Exception e) {
                Toast.makeText(getContext(), "Ошибка шаблона: " + e.getMessage(), Toast.LENGTH_LONG).show();
                //создание пустого шаблона
                templator = new MiniTemplator.Builder().build(new ByteArrayInputStream("Template error!".getBytes(Charset.forName("utf-8"))), Charset.forName("utf-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //init
        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name("forpda.realm")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(configuration);
        Repository.createInstance();
        Client.getInstance();
        initImageLoader(this);
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
        keyboardHeight = getPreferences().getInt("keyboard_height", getContext().getResources().getDimensionPixelSize(R.dimen.default_keyboard_height));
        savedKeyboardHeight = keyboardHeight;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
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

    public static int px2, px4, px6, px8, px12, px14, px16, px24, px32, px36, px40, px48, px56, px64;
}
