package forpdateam.ru.forpda

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import forpdateam.ru.forpda.client.AppImageDownloader
import forpdateam.ru.forpda.common.Html
import forpdateam.ru.forpda.common.apptheme.AppThemeController
import forpdateam.ru.forpda.common.di.AppModule
import forpdateam.ru.forpda.common.receivers.WakeUpReceiver
import forpdateam.ru.forpda.work.WorkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.mintrocket.lib.mintpermissions.ext.initMintPermissions
import ru.radiationx.analytics.Analytics
import ru.radiationx.quill.Quill
import ru.radiationx.quill.get

/**
 * Created by radiationx on 28.07.16.
 */
class App : Application() {

    companion object {

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

    override fun onCreate() {
        super.onCreate()
        val time = System.currentTimeMillis()
        Analytics.initAppMetrica(this, "a94d9236-cdf3-4a5e-af30-d6dbffaea362")

        initDependencies()
        Html.initApplication(this)
        get<AppThemeController>().init()
        initImageLoader(this, get<AppImageDownloader>())
        initMintPermissions()

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

    private fun initDependencies() {
        Quill.getRootScope().installModules(AppModule(this))
    }

    private fun initImageLoader(context: Context, imageDownloader: AppImageDownloader) {
        val defaultOptionsUIL: DisplayImageOptions.Builder = DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .resetViewBeforeLoading(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .handler(Handler(Looper.getMainLooper()))
            .displayer(FadeInBitmapDisplayer(500, true, true, false))

        val config = ImageLoaderConfiguration.Builder(context)
            .imageDownloader(imageDownloader)
            .threadPoolSize(5)
            .threadPriority(Thread.MIN_PRIORITY)
            .denyCacheImageMultipleSizesInMemory()
            .memoryCache(UsingFreqLimitedMemoryCache(5 * 1024 * 1024)) // 5 Mb
            .diskCacheFileNameGenerator(HashCodeFileNameGenerator())
            .defaultDisplayImageOptions(defaultOptionsUIL.build())
            .build()

        ImageLoader.getInstance().init(config)
    }
}
