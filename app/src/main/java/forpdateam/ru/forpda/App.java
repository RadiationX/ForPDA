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
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatDelegate;
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
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;
import com.yandex.metrica.profile.Attribute;
import com.yandex.metrica.profile.GenderAttribute;
import com.yandex.metrica.profile.UserProfile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.common.LocaleHelper;
import forpdateam.ru.forpda.common.realm.DbMigration;
import forpdateam.ru.forpda.common.receivers.NetworkStateReceiver;
import forpdateam.ru.forpda.common.receivers.WakeUpReceiver;
import forpdateam.ru.forpda.common.simple.SimpleObservable;
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel;
import forpdateam.ru.forpda.notifications.NotificationsJob;
import forpdateam.ru.forpda.notifications.NotificationsJobCreator;
import forpdateam.ru.forpda.notifications.NotificationsService;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.Cookie;

/**
 * Created by radiationx on 28.07.16.
 */

public class App extends android.app.Application {
    public static int px2, px4, px6, px8, px12, px14, px16, px20, px24, px32, px36, px40, px48, px56, px64;
    private static App instance;
    private float density = 1.0f;
    private SharedPreferences preferences;

    private SimpleObservable networkForbidden = new SimpleObservable();
    private Boolean webViewFound = null;
    private Messenger mBoundService = null;
    private boolean mServiceBound = false;


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

    public boolean isWebViewFound(Context context) {
        if (webViewFound == null) {
            try {
                WebSettings.getDefaultUserAgent(context);
                webViewFound = true;
            } catch (Exception e) {
                webViewFound = false;
            }
        }
        return webViewFound;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
        if (BuildConfig.FLAVOR.equals("dev")) {
            MultiDex.install(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        long time = System.currentTimeMillis();
        YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder("a94d9236-cdf3-4a5e-af30-d6dbffaea362").build();
        YandexMetrica.activate(getApplicationContext(), config);
        YandexMetrica.enableActivityAutoTracking(this);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }

        //ACRA.init(this);
        dependencies = new Dependencies(this);
        ProfileModel profileModel = dependencies.getUserHolder().getUser();
        String nick = profileModel == null ? "null" : (profileModel.getNick() == null ? "null" : profileModel.getNick());

        UserProfile.Builder userProfileBuilder = UserProfile.newBuilder()
                .apply(Attribute.name().withValue(nick));

        //ACRA.getErrorReporter().putCustomData("USER_NICK", profileModel == null ? "null" : profileModel.getNick());
        RxJavaPlugins.setErrorHandler(throwable -> {
            Log.d("SUKA", "RxJavaPlugins errorHandler " + throwable);
            throwable.printStackTrace();
            YandexMetrica.reportError("Крит " + throwable.getMessage(), throwable);
        });

        setTheme(dependencies.getMainPreferencesHolder().getThemeIsDark() ? R.style.DarkAppTheme : R.style.LightAppTheme);

        try {
            String inputHistory = dependencies.getOtherPreferencesHolder().getAppVersionsHistory();
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
                dependencies.getOtherPreferencesHolder().setAppVersionsHistory(TextUtils.join(";", list));
            }
            if (disorder) {
                throw new Exception("Нарушение порядка версий!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            userProfileBuilder.apply(Attribute.customString("VERSION_HISTORY").withValue(dependencies.getOtherPreferencesHolder().getAppVersionsHistory()));
            YandexMetrica.reportError("VERSIONS_HISTORY", ex);
            //ACRA.getErrorReporter().putCustomData("VERSIONS_HISTORY", dependencies.getOtherPreferencesHolder().getAppVersionsHistory());
            //ACRA.getErrorReporter().handleException(ex);
        }
        userProfileBuilder.apply(Attribute.customString("VERSION_HISTORY").withValue(dependencies.getOtherPreferencesHolder().getAppVersionsHistory()));
        //ACRA.getErrorReporter().putCustomData("VERSIONS_HISTORY", dependencies.getOtherPreferencesHolder().getAppVersionsHistory());

        YandexMetrica.setUserProfileID(nick);
        YandexMetrica.reportUserProfile(userProfileBuilder.build());

        initImageLoader(this);

        updateStaticRes();

        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name("forpda.realm")
                .schemaVersion(4)
                .migration(new DbMigration())
                .build();
        Realm.setDefaultConfiguration(configuration);


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

        //На каких-то диких калькуляторах может быть ANR, поэтому в фоновый поток
        Observable
                .fromCallable(() -> {
                    JobConfig.addLogger((priority, tag, message, t) -> {
                        Log.e("JobLogger", "Job: pr=" + priority + "; t=" + tag + "; m=" + message + "; th=" + t);
                    });
                    JobConfig.setLogcatEnabled(false);
                    JobManager.create(this).addJobCreator(new NotificationsJobCreator());
                    JobManager.instance().cancelAllForTag(NotificationsJob.TAG);
                    new JobRequest.Builder(NotificationsJob.TAG)
                            .setPeriodic(TimeUnit.MINUTES.toMillis(16L))
                            //only non periodic
                            //.setBackoffCriteria(JobRequest.DEFAULT_BACKOFF_MS, JobRequest.BackoffPolicy.LINEAR)
                            .setRequiresCharging(false)
                            .setRequiresDeviceIdle(false)
                            .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                            .build()
                            .schedule();
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        Log.e("APP", "TIME APP FINAL " + (System.currentTimeMillis() - time));

        registerReceiver(
                new NetworkStateReceiver(),
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        );
    }

    private void updateStaticRes() {
        Log.e("kekosina", "updateStaticRes");
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

        HashMap<String, String> templateStringCache = new HashMap<>();
        for (Field f : R.string.class.getFields()) {
            try {
                if (f.getName().contains("res_s_")) {
                    templateStringCache.put(f.getName(), getString(f.getInt(f)));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        dependencies.getTemplateManager().setStaticStrings(templateStringCache);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateStaticRes();
    }

    private Dependencies dependencies;

    public Dependencies Di() {
        return dependencies;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            String n1 = name.getClassName();
            String n2 = NotificationsService.class.getName();
            if (n1.equals(n2)) {
                mBoundService = new Messenger(service);
                mServiceBound = true;
            }
        }
    };

    public ServiceConnection getServiceConnection() {
        return mServiceConnection;
    }

    public static int getToolBarHeight(Context context) {
        int[] attrs = new int[]{R.attr.actionBarSize};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int toolBarHeight = ta.getDimensionPixelSize(0, -1);
        ta.recycle();
        return toolBarHeight;
    }

    public void subscribeForbidden(Observer observer) {
        networkForbidden.addObserver(observer);
    }

    public void unSubscribeForbidden(Observer observer) {
        networkForbidden.deleteObserver(observer);
    }

    public void notifyForbidden(boolean isForbidden) {
        networkForbidden.notifyObservers(isForbidden);
    }

    public int dpToPx(int dp, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
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
                            Map<String, Cookie> cookies = App.get().Di().getWebClient().getClientCookies();
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
