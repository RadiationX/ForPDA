package forpdateam.ru.forpda;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v7.content.res.AppCompatResources;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.webkit.WebSettings;
import android.widget.Toast;

import com.evernote.android.job.JobConfig;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observer;
import java.util.regex.Pattern;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.data.models.TabNotification;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.qms.QmsHelper;
import forpdateam.ru.forpda.notifications.NotificationsJob;
import forpdateam.ru.forpda.notifications.NotificationsJobCreator;
import forpdateam.ru.forpda.notifications.NotificationsService;
import forpdateam.ru.forpda.realm.DbMigration;
import forpdateam.ru.forpda.settings.Preferences;
import forpdateam.ru.forpda.utils.LocaleHelper;
import forpdateam.ru.forpda.utils.SimpleObservable;
import io.reactivex.plugins.RxJavaPlugins;
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
import static org.acra.ReportField.USER_APP_START_DATE;

/**
 * Created by radiationx on 28.07.16.
 */

@ReportsCrashes(
        mailTo = "rxdevlab@gmail.com",
        customReportContent = {APP_VERSION_CODE, APP_VERSION_NAME, ANDROID_VERSION, PHONE_MODEL, CUSTOM_DATA, STACK_TRACE, LOGCAT, USER_APP_START_DATE},
        mode = ReportingInteractionMode.DIALOG,
        resDialogIcon = R.mipmap.ic_launcher,
        resDialogTitle = R.string.crash_title,
        resDialogText = R.string.crash_toast_text,
        resDialogTheme = R.style.DarkAppTheme_Dialog,
        reportDialogClass = CustomCrashDialog.class
)

public class App extends android.app.Application {
    public final static String TEMPLATE_THEME = "theme";
    public final static String TEMPLATE_SEARCH = "search";
    public final static String TEMPLATE_QMS_CHAT = "qms_chat";
    public final static String TEMPLATE_QMS_CHAT_MESS = "qms_chat_mess";
    public final static String TEMPLATE_NEWS = "news";
    public final static String TEMPLATE_FORUM_RULES = "forum_rules";
    public final static String TEMPLATE_ANNOUNCE = "announce";
    public static int px2, px4, px6, px8, px12, px14, px16, px20, px24, px32, px36, px40, px48, px56, px64;
    private static int savedKeyboardHeight = 0;
    public static int keyboardHeight = 0;
    public static int statusBarHeight = 0;
    public static int navigationBarHeight = 0;
    public static HashMap<String, String> templateStringCache = new HashMap<>();
    private static App instance;
    //private final static Object lock = new Object();

    private Locale locale;
    private String lang;
    private Map<String, MiniTemplator> templates = new HashMap<>();
    private float density = 1.0f;
    private SharedPreferences preferences;
    private SimpleObservable preferenceChangeObservables = new SimpleObservable();
    private OnSharedPreferenceChangeListener preferenceChangeListener = (sharedPreferences, key) -> {
        Log.d(App.class.getSimpleName(), "Preference changed: " + key);
        if (key == null) return;
        preferenceChangeObservables.notifyObservers(key);
    };
    private SimpleObservable statusBarSizeObservables = new SimpleObservable();

    private SimpleObservable favoriteEvents = new SimpleObservable();
    private SimpleObservable qmsEvents = new SimpleObservable();


    public App() {
        instance = this;
    }

    public static App get() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    public static Context getContext() {
        return get();
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
    public static int getDrawableResAttr(Context context, @AttrRes int attr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        int attributeResourceId = a.getResourceId(0, 0);
        a.recycle();
        return attributeResourceId;
    }

    public static Drawable getDrawableAttr(Context context, @AttrRes int attr) {
        return AppCompatResources.getDrawable(context, getDrawableResAttr(context, attr));
    }

    public boolean isDarkTheme() {
        return Preferences.Main.Theme.isDark(getContext());
    }

    public String getCssStyleType() {
        return isDarkTheme() ? "dark" : "light";
    }


    public boolean isWebViewFound() {
        try {
            WebSettings.getDefaultUserAgent(App.getContext());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    int mLastJobId;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        long time = System.currentTimeMillis();
        ACRA.init(this);
        ACRA.getErrorReporter().putCustomData("USER_NICK", getPreferences().getString("auth.user.nick", "null"));
        RxJavaPlugins.setErrorHandler(throwable -> {
            Log.d("SUKA", "RxJavaPlugins errorHandler " + throwable);
            throwable.printStackTrace();
        });

        setTheme(isDarkTheme() ? R.style.DarkAppTheme : R.style.LightAppTheme);

        try {
            String inputHistory = getPreferences().getString("app.versions.history", "");
            String[] history = TextUtils.split(inputHistory, ";");

            int lastVNum = 0;
            boolean disorder = false;
            for (String version : history) {
                int vNum = Integer.parseInt(version);
                if (vNum < lastVNum) {
                    disorder = true;
                }
                lastVNum = vNum;
            }
            Object vCode = BuildConfig.VERSION_CODE;
            String sVCode = "" + vCode;
            int nVCode = Integer.parseInt(sVCode);

            if (lastVNum < nVCode) {
                List<String> list = new ArrayList<>(Arrays.asList(history));
                list.add(Integer.toString(nVCode));
                getPreferences().edit().putString("app.versions.history", TextUtils.join(";", list)).apply();
            }
            if (disorder) {
                throw new Exception("Нарушение порядка версий!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ACRA.getErrorReporter().putCustomData("VERSIONS_HISTORY", getPreferences().getString("app.versions.history", ""));
            ACRA.getErrorReporter().handleException(ex);
        }

        ACRA.getErrorReporter().putCustomData("VERSIONS_HISTORY", getPreferences().getString("app.versions.history", ""));

        density = getResources().getDisplayMetrics().density;
        initImageLoader(this);

        px2 = getContext().getResources().getDimensionPixelSize(R.dimen.dp2);
        px4 = getContext().getResources().getDimensionPixelSize(R.dimen.dp4);
        px6 = getContext().getResources().getDimensionPixelSize(R.dimen.dp6);
        px8 = getContext().getResources().getDimensionPixelSize(R.dimen.dp8);
        px12 = getContext().getResources().getDimensionPixelSize(R.dimen.dp12);
        px14 = getContext().getResources().getDimensionPixelSize(R.dimen.dp14);
        px16 = getContext().getResources().getDimensionPixelSize(R.dimen.dp16);
        px20 = getContext().getResources().getDimensionPixelSize(R.dimen.dp20);
        px24 = getContext().getResources().getDimensionPixelSize(R.dimen.dp24);
        px32 = getContext().getResources().getDimensionPixelSize(R.dimen.dp32);
        px36 = getContext().getResources().getDimensionPixelSize(R.dimen.dp36);
        px40 = getContext().getResources().getDimensionPixelSize(R.dimen.dp40);
        px48 = getContext().getResources().getDimensionPixelSize(R.dimen.dp48);
        px56 = getContext().getResources().getDimensionPixelSize(R.dimen.dp56);
        px64 = getContext().getResources().getDimensionPixelSize(R.dimen.dp64);

        for (Field f : R.string.class.getFields()) {
            try {
                if (f.getName().contains("res_s_")) {
                    templateStringCache.put(f.getName(), getString(f.getInt(f)));
                }
            } catch (Exception ignore) {
            }
        }

        keyboardHeight = getPreferences().getInt("keyboard_height", getContext().getResources().getDimensionPixelSize(R.dimen.default_keyboard_height));
        savedKeyboardHeight = keyboardHeight;

        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name("forpda.realm")
                .schemaVersion(3)
                .migration(new DbMigration())
                .build();
        Realm.setDefaultConfiguration(configuration);
        Client.get(getContext());


        getPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(App.class.getSimpleName(), "DOZE ON RECEIVE " + intent);
                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    if (pm == null)
                        return;
                    if (pm.isDeviceIdleMode()) {
                        // the device is now in doze mode
                        Log.d(App.class.getSimpleName(), "DOZE MODE ENABLYA");
                    } else {
                        // the device just woke up from doze mode
                        Log.d(App.class.getSimpleName(), "DOZE MODE DISABLYA");
                        NotificationsService.startAndCheck();
                    }
                }
            };

            registerReceiver(receiver, new IntentFilter(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED));
        }


        IntentFilter wakeUpFilter = new IntentFilter();
        wakeUpFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        wakeUpFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(new WakeUpReceiver(), wakeUpFilter);


        JobConfig.addLogger((priority, tag, message, t) -> {
            Log.e("SUKA", "Job: pr=" + priority + "; t=" + tag + "; m=" + message + "; th=" + t);
        });
        JobConfig.setLogcatEnabled(false);
        JobManager.create(this).addJobCreator(new NotificationsJobCreator());
        JobManager.instance().cancelAllForTag(NotificationsJob.TAG);
        mLastJobId = new JobRequest.Builder(NotificationsJob.TAG)
                .setPeriodic(JobRequest.MIN_INTERVAL)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                .build()
                .schedule();
        QmsHelper.init();
        Log.e("SUKAA", "TIME APP FINAL " + (System.currentTimeMillis() - time));
    }

    private NotificationsService mBoundService;
    boolean mServiceBound = false;

    public ServiceConnection getmServiceConnection() {
        return mServiceConnection;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof NotificationsService.MyBinder) {
                NotificationsService.MyBinder myBinder = (NotificationsService.MyBinder) service;
                mBoundService = myBinder.getService();
                mServiceBound = true;
            } else {
                ACRA.getErrorReporter().handleException(new Throwable("What's wrong, bro? IBinder=" + service));
            }
        }
    };

    public static HashMap<String, String> getTemplateStringCache() {
        return templateStringCache;
    }

    public static MiniTemplator setTemplateResStrings(MiniTemplator t) {
        for (Map.Entry<String, String> entry : t.getVariables().entrySet()) {
            String cacheValue = App.getTemplateStringCache().get(entry.getKey());
            if (cacheValue != null) {
                t.setVariable(entry.getKey(), cacheValue);
            }
        }
        return t;
    }

    public static int getToolBarHeight(Context context) {
        int[] attrs = new int[]{R.attr.actionBarSize};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int toolBarHeight = ta.getDimensionPixelSize(0, -1);
        ta.recycle();
        return toolBarHeight;
    }


    public void subscribeFavorites(Observer observer) {
        favoriteEvents.addObserver(observer);
    }

    public void unSubscribeFavorites(Observer observer) {
        favoriteEvents.deleteObserver(observer);
    }

    public void notifyFavorites(TabNotification event) {
        favoriteEvents.notifyObservers(event);
    }

    public void subscribeQms(Observer observer) {
        qmsEvents.addObserver(observer);
    }

    public void unSubscribeQms(Observer observer) {
        qmsEvents.deleteObserver(observer);
    }

    public void notifyQms(TabNotification event) {
        qmsEvents.notifyObservers(event);
    }

    public void addPreferenceChangeObserver(Observer observer) {
        preferenceChangeObservables.addObserver(observer);
    }

    public void removePreferenceChangeObserver(Observer observer) {
        preferenceChangeObservables.deleteObserver(observer);
    }

    public void addStatusBarSizeObserver(Observer observer) {
        statusBarSizeObservables.addObserver(observer);
        observer.update(statusBarSizeObservables, null);
    }

    public void removeStatusBarSizeObserver(Observer observer) {
        statusBarSizeObservables.deleteObserver(observer);
    }

    public SimpleObservable getStatusBarSizeObservables() {
        return statusBarSizeObservables;
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
        Log.d("FORPDA_LOG", "setKeyboardHeight " + newKeyboardHeight);
        keyboardHeight = newKeyboardHeight;
        if (keyboardHeight == savedKeyboardHeight) return;
        App.get().getPreferences().edit().putInt("keyboard_height", keyboardHeight).apply();
    }


    public MiniTemplator getTemplate(String name) {
        MiniTemplator template = templates.get(name);
        if (template == null) {
            template = findTemplate(name);
            if (template != null) {
                templates.put(name, template);
            }
        }
        return template;
    }

    private MiniTemplator findTemplate(String name) {
        MiniTemplator template = null;
        try {
            InputStream stream = App.get().getAssets().open("template_".concat(name).concat(".html"));
            try {
                template = new MiniTemplator.Builder().build(stream, Charset.forName("utf-8"));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), String.format(getString(R.string.template_error), name, e.getMessage()), Toast.LENGTH_LONG).show();
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

    public static Drawable getAppVecDrawable(@DrawableRes int id) {
        return AppCompatResources.getDrawable(App.getContext(), id);
    }

    /*Only vector icon*/
    public static Drawable getVecDrawable(Context context, @DrawableRes int id) {
        Drawable drawable = AppCompatResources.getDrawable(context, id);
        if (!(drawable instanceof VectorDrawableCompat || drawable instanceof VectorDrawable)) {
            throw new RuntimeException();
        }
        return drawable;
    }

    private static DisplayImageOptions.Builder options = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .resetViewBeforeLoading(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .handler(new Handler())
            .displayer(new FadeInBitmapDisplayer(500, true, true, false));

    public static DisplayImageOptions.Builder getDefaultOptionsUIL() {
        return options;
    }

    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .imageDownloader(new BaseImageDownloader(context) {
                    final Pattern pattern4pda = Pattern.compile("(?:http?s?:)?\\/\\/.*?4pda\\.(?:ru|to)");

                    @Override
                    public InputStream getStream(String imageUri, Object extra) throws IOException {
                        if (imageUri.substring(0, 2).equals("//"))
                            imageUri = "http:".concat(imageUri);
                        Log.d(App.class.getSimpleName(), "ImageLoader getStream " + imageUri);
                        return super.getStream(imageUri, extra);
                    }

                    @Override
                    protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
                        HttpURLConnection conn = super.createConnection(url, extra);
                        if (pattern4pda.matcher(url).find()) {
                            Map<String, Cookie> cookies = Client.get().getClientCookies();
                            String stringCookies = "";
                            for (Map.Entry<String, Cookie> cookieEntry : cookies.entrySet()) {
                                stringCookies = stringCookies.concat(cookieEntry.getKey()).concat("=").concat(cookieEntry.getValue().value()).concat(";");
                            }
                            conn.setRequestProperty("Cookie", stringCookies);
                        }
                        return conn;
                    }
                })
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(5 * 1024 * 1024)) // 5 Mb
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .defaultDisplayImageOptions(options.build())
                .build();

        ImageLoader.getInstance().init(config);
    }


    public SharedPreferences getPreferences() {
        if (preferences == null)
            preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return preferences;
    }

    public static SharedPreferences getPreferences(Context context) {
        if (context == null) {
            return App.get().getPreferences();
        }
        return PreferenceManager.getDefaultSharedPreferences(context);
    }


    public static Activity getActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);

            Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
            if (activities == null)
                return null;

            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private List<Runnable> permissionCallbacks = new ArrayList<>();

    public void checkStoragePermission(Runnable runnable, Activity activity) {
        if (runnable == null || activity == null)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, TabFragment.REQUEST_STORAGE);
                permissionCallbacks.add(runnable);
                return;
            }
        }
        runnable.run();
    }

    //PLS CALL THIS IN ALL ACTIVITIES
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                for (Runnable runnable : permissionCallbacks) {
                    try {
                        runnable.run();
                    } catch (Exception ignore) {
                    }
                }
                break;
            }
        }
        permissionCallbacks.clear();
    }
}
