package forpdateam.ru.forpda

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Handler
import android.os.Looper
import android.os.Messenger
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.webkit.WebSettings
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import com.nostra13.universalimageloader.core.download.BaseImageDownloader
import forpdateam.ru.forpda.R.string
import forpdateam.ru.forpda.common.AppBuildConfig
import forpdateam.ru.forpda.common.DayNightHelper
import forpdateam.ru.forpda.common.LocaleHelper
import forpdateam.ru.forpda.common.receivers.WakeUpReceiver
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.work.WorkUtils
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.Arrays

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
            .handler(Handler(Looper.getMainLooper()))
            .displayer(FadeInBitmapDisplayer(500, true, true, false))

        fun initImageLoader(context: Context) {
            val config = ImageLoaderConfiguration.Builder(context)
                .imageDownloader(object : BaseImageDownloader(context) {

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
                        val cookies = get().Di().cookieJar.loadForRequest(url.toHttpUrl())
                        if (cookies.isNotEmpty()) {
                            val headerValue = cookies.joinToString(separator = ";") {
                                "${it.name}=${it.value}"
                            }
                            conn.setRequestProperty("Cookie", headerValue)
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
        val config = AppMetricaConfig.newConfigBuilder("a94d9236-cdf3-4a5e-af30-d6dbffaea362").build()
        AppMetrica.activate(applicationContext, config)
        AppMetrica.enableActivityAutoTracking(this)

        dependencies
            .mainPreferencesHolder
            .themeMode
            .onEach {
                DayNightHelper.applyTheme(it)
            }
            .launchIn(GlobalScope + Dispatchers.Main)

        try {
            val inputHistory = dependencies.otherPreferencesHolder.appVersionsHistory.get().orEmpty()
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
            val vCode: Int = AppBuildConfig.versionCode
            val sVCode = "" + vCode
            val nVCode = sVCode.toInt()

            if (lastVNum < nVCode) {
                val list: MutableList<String?> = ArrayList(Arrays.asList(*history))
                list.add(nVCode.toString())
                dependencies.otherPreferencesHolder.appVersionsHistory.set(
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
            AppMetrica.reportError("VERSIONS_HISTORY", ex)
        }

        initImageLoader(this)

        updateStaticRes()


        val wakeUpFilter = IntentFilter()
        wakeUpFilter.addAction(Intent.ACTION_BOOT_COMPLETED)
        wakeUpFilter.addAction(Intent.ACTION_SCREEN_ON)
        registerReceiver(WakeUpReceiver(), wakeUpFilter)

        //На каких-то диких калькуляторах может быть ANR, поэтому в фоновый поток
        GlobalScope.launch(Dispatchers.Default) {
            WorkUtils.enqueuePeriodicInspectorCheck(this@App)
        }

        Log.e("APP", "TIME APP FINAL " + (System.currentTimeMillis() - time))
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

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                TabFragment.REQUEST_STORAGE
            )
            permissionCallbacks.add(runnable)
            return
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
