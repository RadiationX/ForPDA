package forpdateam.ru.forpda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.RequiresApi;
import android.support.v7.content.res.AppCompatResources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.data.Repository;
import forpdateam.ru.forpda.settings.Preferences;
import forpdateam.ru.forpda.utils.SimpleObservable;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.Cookie;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_CODE;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.CUSTOM_DATA;
import static org.acra.ReportField.LOGCAT;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.STACK_TRACE;

/**
 * Created by radiationx on 28.07.16.
 */

@ReportsCrashes(
        mailTo = "rxdevlab@gmail.com",
        customReportContent = {APP_VERSION_CODE, APP_VERSION_NAME, ANDROID_VERSION, PHONE_MODEL, CUSTOM_DATA, STACK_TRACE, LOGCAT},
        mode = ReportingInteractionMode.NOTIFICATION,
        resNotifTickerText = R.string.crash_notif_ticker_text,
        resNotifTitle = R.string.crash_notif_title,
        resNotifText = R.string.crash_notif_text,
        resNotifIcon = android.R.drawable.stat_notify_error,
        resDialogText = R.string.crash_toast_text
)

public class App extends android.app.Application {
    public final static String TEMPLATE_THEME = "theme";
    public final static String TEMPLATE_SEARCH = "search";
    public final static String TEMPLATE_QMS_CHAT = "qms_chat";
    public final static String TEMPLATE_QMS_CHAT_MESS = "qms_chat_mess";
    public static int px2, px4, px6, px8, px12, px14, px16, px24, px32, px36, px40, px48, px56, px64;
    private static int savedKeyboardHeight = 0;
    public static int keyboardHeight = 0;
    public static int statusBarHeight = 0;
    public static int navigationBarHeight = 0;
    public static SparseArray<Drawable> drawableCache = new SparseArray<>();
    private static App instance;
    private final static Object lock = new Object();

    private Map<String, MiniTemplator> templates = new HashMap<>();
    private float density = 1.0f;
    private SharedPreferences preferences;
    private SimpleObservable preferenceChangeObservables = new SimpleObservable();
    private OnSharedPreferenceChangeListener preferenceChangeListener = (sharedPreferences, key) -> {
        Log.e("SUKA", "PREFERENCE CHANGED2 " + key);
        if (key == null) return;
        preferenceChangeObservables.notifyObservers(key);
    };


    public static App getInstance() {
        if (instance == null) {
            synchronized (lock) {
                System.out.print("SUKA sync init APP instance " + instance);
                if (instance == null)
                    instance = new App();
            }
        }
        return instance;
    }

    public static Context getContext() {
        return getInstance();
    }

    @ColorInt
    public static int getColorFromAttr(Context context, @AttrRes int attr) {
        TypedValue typedValue = new TypedValue();
        if (context != null && context.getTheme().resolveAttribute(attr, typedValue, true))
            return typedValue.data;
        else
            return Color.RED;
    }

    @DrawableRes
    public static int getDrawableFromAttr(Context context, @AttrRes int attr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        int attributeResourceId = a.getResourceId(0, 0);
        a.recycle();
        return attributeResourceId;
    }

    public boolean isDarkTheme() {
        return getPreferences().getBoolean(Preferences.Main.Theme.IS_DARK, false);
    }

    public String getCssStyleType() {
        return isDarkTheme() ? "dark" : "light";
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.LightAppTheme);
        instance = this;
        ACRA.init(this);
        ACRA.getErrorReporter().putCustomData("USER_NICK", getPreferences().getString("auth.user.nick", "null"));
        density = getResources().getDisplayMetrics().density;

        templates.put(TEMPLATE_THEME, findTemplate(TEMPLATE_THEME));
        templates.put(TEMPLATE_SEARCH, findTemplate(TEMPLATE_SEARCH));
        templates.put(TEMPLATE_QMS_CHAT, findTemplate(TEMPLATE_QMS_CHAT));
        templates.put(TEMPLATE_QMS_CHAT_MESS, findTemplate(TEMPLATE_QMS_CHAT_MESS));

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
                    drawableCache.put(f.getInt(f), AppCompatResources.getDrawable(App.getContext(), f.getInt(f)));
            } catch (Exception ignore) {
            }
        }
        keyboardHeight = getPreferences().getInt("keyboard_height", getContext().getResources().getDimensionPixelSize(R.dimen.default_keyboard_height));
        savedKeyboardHeight = keyboardHeight;

        getPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        Intent serviceIntent = new Intent(App.getContext(), WebSocketService.class).setAction("SOSNO HUICA");
        startService(serviceIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onReceive(Context context, Intent intent) {
                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

                    if (pm.isDeviceIdleMode()) {
                        // the device is now in doze mode
                        Log.e("4DOZE", "DOZE MODE ENABLYA");
                    } else {
                        // the device just woke up from doze mode
                        Log.e("4DOZE", "DOZE MODE DISABLYA");
                        startService(new Intent(App.getContext(), WebSocketService.class).setAction(WebSocketService.CHECK_LAST_EVENTS));
                    }
                }
            };

            registerReceiver(receiver, new IntentFilter(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED));
        }
    }

    public void addPreferenceChangeObserver(Observer observer) {
        preferenceChangeObservables.addObserver(observer);
    }

    public void removePreferenceChangeObserver(Observer observer) {
        preferenceChangeObservables.addObserver(observer);
    }

    public static int getStatusBarHeight() {
        return statusBarHeight;
    }

    public static void setStatusBarHeight(int statusBarHeight) {
        App.statusBarHeight = statusBarHeight;
    }

    public static int getNavigationBarHeight() {
        return navigationBarHeight;
    }

    public static void setNavigationBarHeight(int navigationBarHeight) {
        App.navigationBarHeight = navigationBarHeight;
    }

    public static int getKeyboardHeight() {
        return keyboardHeight;
    }

    public static void setKeyboardHeight(int newKeyboardHeight) {
        keyboardHeight = newKeyboardHeight;
        if (keyboardHeight == savedKeyboardHeight) return;
        App.getInstance().getPreferences().edit().putInt("keyboard_height", keyboardHeight).apply();
    }


    public MiniTemplator getTemplate(String name) {
        return templates.get(name);
    }

    private MiniTemplator findTemplate(String name) {
        MiniTemplator template = null;
        try {
            InputStream stream = App.getInstance().getAssets().open("template_".concat(name).concat(".html"));
            try {
                template = new MiniTemplator.Builder().build(stream, Charset.forName("utf-8"));
            } catch (Exception e) {
                Toast.makeText(getContext(), "Ошибка шаблона [" + name + "]: " + e.getMessage(), Toast.LENGTH_LONG).show();
                //создание пустого шаблона
                template = new MiniTemplator.Builder().build(new ByteArrayInputStream("Template error!".getBytes(Charset.forName("utf-8"))), Charset.forName("utf-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return template;
    }


    public float getDensity() {
        return density;
    }


    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int convertSpToPixels(float sp) {
        Log.d("SUKA", "SP: " + sp);
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getContext().getResources().getDisplayMetrics());
        Log.d("SUKA", "PX: " + px);
        return px;
    }


    public static Drawable getAppDrawable(int id) {
        //return drawableCache.get(id);
        return AppCompatResources.getDrawable(App.getContext(), id);
    }

    public static Drawable getAppDrawable(Context context, int id) {
        //return drawableCache.get(id);
        return AppCompatResources.getDrawable(context, id);
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
                .imageDownloader(new BaseImageDownloader(context) {
                    @Override
                    public InputStream getStream(String imageUri, Object extra) throws IOException {
                        if (imageUri.substring(0, 2).equals("//"))
                            imageUri = "http:".concat(imageUri);
                        Log.d("FORPDA_LOG", "UIL LOAD IMAGE: ".concat(imageUri));
                        return super.getStream(imageUri, extra);
                    }

                    @Override
                    protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
                        HttpURLConnection conn = super.createConnection(url, extra);
                        Map<String, Cookie> cookies = Client.getInstance().getCookies();
                        String stringCookies = "";
                        for (Map.Entry<String, Cookie> cookieEntry : cookies.entrySet()) {
                            stringCookies = stringCookies.concat(cookieEntry.getKey()).concat("=").concat(cookieEntry.getValue().value()).concat(";");
                        }
                        conn.setRequestProperty("Cookie", stringCookies);
                        return conn;
                    }
                })
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(5 * 1024 * 1024)) // 5 Mb
                .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .defaultDisplayImageOptions(options.build())
                .build();

        ImageLoader.getInstance().init(config);
    }


    public SharedPreferences getPreferences() {
        if (preferences == null)
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences;
    }
}
