package forpdateam.ru.forpda

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Messenger
import android.os.PowerManager
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.webkit.WebSettings
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.evernote.android.job.JobConfig
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import com.nostra13.universalimageloader.core.download.BaseImageDownloader
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import forpdateam.ru.forpda.R.string
import forpdateam.ru.forpda.common.DayNightHelper
import forpdateam.ru.forpda.common.LocaleHelper
import forpdateam.ru.forpda.common.Preferences.Main.ThemeMode
import forpdateam.ru.forpda.common.receivers.NetworkStateReceiver
import forpdateam.ru.forpda.common.receivers.WakeUpReceiver
import forpdateam.ru.forpda.common.simple.SimpleObservable
import forpdateam.ru.forpda.notifications.NotificationsJob
import forpdateam.ru.forpda.notifications.NotificationsJobCreator
import forpdateam.ru.forpda.notifications.NotificationsService
import forpdateam.ru.forpda.ui.fragments.TabFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.Arrays
import java.util.Observer
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Created by radiationx on 28.07.16.
 */
class App : Application() {

    companion object {
        var px2: Int = 0
        var px4: Int = 0
        var px6: Int = 0

        @JvmField
        var px8: Int = 0

        @JvmField
        var px12: Int = 0
        var px14: Int = 0

        @JvmField
        var px16: Int = 0
        var px20: Int = 0

        @JvmField
        var px24: Int = 0
        var px32: Int = 0
        var px36: Int = 0
        var px40: Int = 0

        @JvmField
        var px48: Int = 0

        @JvmField
        var px56: Int = 0
        var px64: Int = 0
        private var instance: App? = null

        @JvmStatic
        fun get(): App {
            if (instance == null) {
                instance = App()
            }
            return requireNotNull(instance)
        }

        @JvmStatic
        fun getContext(): Context {
            return get()
        }

        @JvmStatic
        @ColorInt
        fun getColorFromAttr(context: Context?, @AttrRes attr: Int): Int {
            val typedValue = TypedValue()
            return if (context != null && context.theme.resolveAttribute(
                    attr,
                    typedValue,
                    true
                )
            ) typedValue.data
            else Color.RED
        }

        @JvmStatic
        @DrawableRes
        fun getDrawableResAttr(context: Context, @AttrRes attr: Int): Int {
            val a = context.theme.obtainStyledAttributes(intArrayOf(attr))
            val attributeResourceId = a.getResourceId(0, 0)
            a.recycle()
            return attributeResourceId
        }

        @JvmStatic
        fun getDrawableAttr(context: Context, @AttrRes attr: Int): Drawable? {
            return AppCompatResources.getDrawable(context, getDrawableResAttr(context, attr))
        }

        @JvmStatic
        fun getToolBarHeight(context: Context): Int {
            val attrs = intArrayOf(R.attr.actionBarSize)
            val ta = context.obtainStyledAttributes(attrs)
            val toolBarHeight = ta.getDimensionPixelSize(0, -1)
            ta.recycle()
            return toolBarHeight
        }

        /*Only vector icon*/
        @JvmStatic
        fun getVecDrawable(context: Context, @DrawableRes id: Int): Drawable {
            val drawable = AppCompatResources.getDrawable(context, id)
            if (!(drawable is VectorDrawableCompat || drawable is VectorDrawable)) {
                throw RuntimeException()
            }
            return drawable
        }

        val defaultOptionsUIL: DisplayImageOptions.Builder = DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .resetViewBeforeLoading(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .handler(Handler())
            .displayer(FadeInBitmapDisplayer(500, true, true, false))

        fun initImageLoader(context: Context) {
            val config = ImageLoaderConfiguration.Builder(context)
                .imageDownloader(object : BaseImageDownloader(context) {
                    val pattern4pda: Pattern =
                        Pattern.compile("(?:http?s?:)?\\/\\/.*?4pda\\.(?:ru|to)")

                    @Throws(IOException::class)
                    override fun getStream(imageUri: String, extra: Any?): InputStream {
                        var imageUri = imageUri
                        if (imageUri.startsWith("//")) imageUri = "http:$imageUri"
                        Log.d(
                            App::class.java.simpleName,
                            "ImageLoader getStream $imageUri"
                        )
                        return super.getStream(imageUri, extra)
                    }

                    @Throws(IOException::class)
                    override fun createConnection(url: String, extra: Any?): HttpURLConnection {
                        val conn = super.createConnection(url, extra)
                        if (pattern4pda.matcher(url).find()) {
                            val cookies = get().Di().webClient.getClientCookies()
                            var stringCookies = ""
                            for ((key, value) in cookies) {
                                stringCookies = stringCookies + key + "=" + value.value + ";"
                            }
                            conn.setRequestProperty("Cookie", stringCookies)
                        }
                        return conn
                    }
                })
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(UsingFreqLimitedMemoryCache(5 * 1024 * 1024)) // 5 Mb
                .diskCacheFileNameGenerator(HashCodeFileNameGenerator())
                .defaultDisplayImageOptions(defaultOptionsUIL.build())
                .build()

            ImageLoader.getInstance().init(config)
        }


        fun getActivity(): Activity? {
            try {
                val activityThreadClass = Class.forName("android.app.ActivityThread")
                val activityThread =
                    activityThreadClass.getMethod("currentActivityThread").invoke(null)
                val activitiesField = activityThreadClass.getDeclaredField("mActivities")
                activitiesField.isAccessible = true

                val activities = activitiesField[activityThread] as? Map<Any, Any>? ?: return null

                for (activityRecord in activities.values) {
                    val activityRecordClass: Class<*> = activityRecord.javaClass
                    val pausedField =
                        activityRecordClass.getDeclaredField("paused")
                    pausedField.isAccessible = true
                    if (!pausedField.getBoolean(activityRecord)) {
                        val activityField =
                            activityRecordClass.getDeclaredField("activity")
                        activityField.isAccessible = true
                        val activity = activityField[activityRecord] as Activity
                        return activity
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }
    }

    val preferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private val networkForbidden = SimpleObservable()
    private var webViewFound: Boolean? = null
    private var mBoundService: Messenger? = null
    private var mServiceBound = false


    fun isWebViewFound(context: Context?): Boolean {
        if (webViewFound == null) {
            try {
                WebSettings.getDefaultUserAgent(context)
                webViewFound = true
            } catch (e: Exception) {
                webViewFound = false
            }
        }
        return requireNotNull(webViewFound)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        val time = System.currentTimeMillis()
        val config =
            YandexMetricaConfig.newConfigBuilder("a94d9236-cdf3-4a5e-af30-d6dbffaea362").build()
        YandexMetrica.activate(applicationContext, config)
        YandexMetrica.enableActivityAutoTracking(this)


        RxJavaPlugins.setErrorHandler { throwable: Throwable ->
            Log.d(
                "SUKA",
                "RxJavaPlugins errorHandler $throwable"
            )
            throwable.printStackTrace()
            YandexMetrica.reportError("Крит " + throwable.message, throwable)
        }

        val disposable = dependencies
            .mainPreferencesHolder
            .observeThemeMode()
            .distinctUntilChanged()
            .subscribe(
                { mode: ThemeMode ->
                    DayNightHelper.applyTheme(mode)
                },
                { obj: Throwable -> obj.printStackTrace() }
            )

        try {
            val inputHistory = dependencies.otherPreferencesHolder.getAppVersionsHistory()
            val history = TextUtils.split(inputHistory, ";")

            var lastVNum = 0
            var disorder = false
            for (version in history) {
                val vNum = version.toInt()
                if (vNum < lastVNum) {
                    disorder = true
                }
                lastVNum = vNum
            }
            val vCode: Int = BuildConfig.VERSION_CODE
            val sVCode = "" + vCode
            val nVCode = sVCode.toInt()

            if (lastVNum < nVCode) {
                val list: MutableList<String?> = ArrayList(Arrays.asList(*history))
                list.add(nVCode.toString())
                dependencies.otherPreferencesHolder.setAppVersionsHistory(
                    TextUtils.join(
                        ";",
                        list
                    )
                )
            }
            if (disorder) {
                throw Exception("Нарушение порядка версий!")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            YandexMetrica.reportError("VERSIONS_HISTORY", ex)
        }

        initImageLoader(this)

        updateStaticRes()




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val receiver: BroadcastReceiver = object : BroadcastReceiver() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                override fun onReceive(context: Context, intent: Intent) {
                    Log.d(App::class.java.simpleName, "DOZE ON RECEIVE $intent")
                    val pm =
                        context.getSystemService(POWER_SERVICE) as PowerManager
                            ?: return
                    if (pm.isDeviceIdleMode) {
                        // the device is now in doze mode
                        Log.d(App::class.java.simpleName, "DOZE MODE ENABLYA")
                    } else {
                        // the device just woke up from doze mode
                        Log.d(App::class.java.simpleName, "DOZE MODE DISABLYA")
                        NotificationsService.startAndCheck()
                    }
                }
            }

            registerReceiver(receiver, IntentFilter(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED))
        }


        val wakeUpFilter = IntentFilter()
        wakeUpFilter.addAction(Intent.ACTION_BOOT_COMPLETED)
        wakeUpFilter.addAction(Intent.ACTION_SCREEN_ON)
        registerReceiver(WakeUpReceiver(), wakeUpFilter)

        //На каких-то диких калькуляторах может быть ANR, поэтому в фоновый поток
        Observable
            .fromCallable {
                JobConfig.addLogger { priority: Int, tag: String, message: String, t: Throwable? ->
                    Log.e(
                        "JobLogger",
                        "Job: pr=$priority; t=$tag; m=$message; th=$t"
                    )
                }
                JobConfig.setLogcatEnabled(false)
                JobManager.create(this).addJobCreator(NotificationsJobCreator())
                JobManager.instance().cancelAllForTag(NotificationsJob.TAG)
                JobRequest.Builder(NotificationsJob.TAG)
                    .setPeriodic(TimeUnit.MINUTES.toMillis(16L)) //only non periodic
                    //.setBackoffCriteria(JobRequest.DEFAULT_BACKOFF_MS, JobRequest.BackoffPolicy.LINEAR)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                    .build()
                    .schedule()
                true
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()

        Log.e("APP", "TIME APP FINAL " + (System.currentTimeMillis() - time))

        registerReceiver(
            NetworkStateReceiver(),
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    private fun updateStaticRes() {
        Log.e("kekosina", "updateStaticRes")
        px2 = getContext().resources.getDimensionPixelSize(R.dimen.dp2)
        px4 = getContext().resources.getDimensionPixelSize(R.dimen.dp4)
        px6 = getContext().resources.getDimensionPixelSize(R.dimen.dp6)
        px8 = getContext().resources.getDimensionPixelSize(R.dimen.dp8)
        px12 = getContext().resources.getDimensionPixelSize(R.dimen.dp12)
        px14 = getContext().resources.getDimensionPixelSize(R.dimen.dp14)
        px16 = getContext().resources.getDimensionPixelSize(R.dimen.dp16)
        px20 = getContext().resources.getDimensionPixelSize(R.dimen.dp20)
        px24 = getContext().resources.getDimensionPixelSize(R.dimen.dp24)
        px32 = getContext().resources.getDimensionPixelSize(R.dimen.dp32)
        px36 = getContext().resources.getDimensionPixelSize(R.dimen.dp36)
        px40 = getContext().resources.getDimensionPixelSize(R.dimen.dp40)
        px48 = getContext().resources.getDimensionPixelSize(R.dimen.dp48)
        px56 = getContext().resources.getDimensionPixelSize(R.dimen.dp56)
        px64 = getContext().resources.getDimensionPixelSize(R.dimen.dp64)

        val templateStringCache = HashMap<String, String>()
        for (f in string::class.java.fields) {
            try {
                if (f.name.contains("res_s_")) {
                    templateStringCache[f.name] = getString(f.getInt(f))
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        dependencies.templateManager.setStaticStrings(templateStringCache)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateStaticRes()
    }

    private val dependencies by lazy { Dependencies(this) }

    fun Di(): Dependencies {
        return dependencies
    }

    val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mBoundService = null
            mServiceBound = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val n1 = name.className
            val n2 = NotificationsService::class.java.name
            if (n1 == n2) {
                mBoundService = Messenger(service)
                mServiceBound = true
            }
        }
    }

    fun subscribeForbidden(observer: Observer?) {
        networkForbidden.addObserver(observer)
    }

    fun unSubscribeForbidden(observer: Observer?) {
        networkForbidden.deleteObserver(observer)
    }

    fun notifyForbidden(isForbidden: Boolean) {
        networkForbidden.notifyObservers(isForbidden)
    }

    fun dpToPx(dp: Int, context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    private val permissionCallbacks: MutableList<Runnable> = ArrayList()

    init {
        instance = this
    }

    fun checkStoragePermission(runnable: Runnable?, activity: Activity?) {
        if (runnable == null || activity == null) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    TabFragment.REQUEST_STORAGE
                )
                permissionCallbacks.add(runnable)
                return
            }
        }
        runnable.run()
    }

    //PLS CALL THIS IN ALL ACTIVITIES
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        for (i in permissions.indices) {
            if (permissions[i] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                for (runnable in permissionCallbacks) {
                    try {
                        runnable.run()
                    } catch (ignore: Exception) {
                    }
                }
                break
            }
        }
        permissionCallbacks.clear()
    }

}
