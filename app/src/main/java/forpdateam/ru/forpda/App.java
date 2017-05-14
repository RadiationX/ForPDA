package forpdateam.ru.forpda;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.content.res.AppCompatResources;
import android.util.DisplayMetrics;
import android.util.Log;
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
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.data.Repository;
import io.realm.Realm;
import io.realm.RealmConfiguration;

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
    private static App INSTANCE = new App();
    private SharedPreferences preferences;
    private static int savedKeyboardHeight = 0;
    public static int keyboardHeight = 0;
    public static int statusBarHeight = 0;
    public static int navigationBarHeight = 0;

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

    public static App getInstance() {
        return INSTANCE;
    }

    public App() {
        INSTANCE = this;
    }


    private Map<String, MiniTemplator> templates = new HashMap<>();

    public MiniTemplator getTemplate(String name){
        return templates.get(name);
    }

    private MiniTemplator findTemplate(String name) {
        MiniTemplator template = null;
        try {
            InputStream stream = App.getInstance().getAssets().open("template_".concat(name).concat(".html"));
            try {
                template = new MiniTemplator.Builder().build(stream, Charset.forName("utf-8"));
            } catch (Exception e) {
                Toast.makeText(getContext(), "Ошибка шаблона ["+name+"]: " + e.getMessage(), Toast.LENGTH_LONG).show();
                //создание пустого шаблона
                template = new MiniTemplator.Builder().build(new ByteArrayInputStream("Template error!".getBytes(Charset.forName("utf-8"))), Charset.forName("utf-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return template;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);

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
                .imageDownloader(new BaseImageDownloader(context) {
                    @Override
                    public InputStream getStream(String imageUri, Object extra) throws IOException {
                        if (imageUri.substring(0, 2).equals("//"))
                            imageUri = "http:".concat(imageUri);
                        Log.d("FORPDA_LOG", "UIL LOAD IMAGE: ".concat(imageUri));
                        return super.getStream(imageUri, extra);
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
