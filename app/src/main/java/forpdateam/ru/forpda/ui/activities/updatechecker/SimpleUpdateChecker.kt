package forpdateam.ru.forpda.ui.activities.updatechecker

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.AppBuildConfig
import forpdateam.ru.forpda.entity.app.checker.UpdateData
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.extensions.immutableFlag
import forpdateam.ru.forpda.model.preferences.NotificationPreferencesHolder
import forpdateam.ru.forpda.model.repository.checker.CheckerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.mintrocket.lib.mintpermissions.MintPermissionsController
import ru.mintrocket.lib.mintpermissions.ext.isGranted

/**
 * Created by radiationx on 23.07.17.
 */

class SimpleUpdateChecker(
    private val context: Context,
    private val checkerRepository: CheckerRepository,
    private val permissionsController: MintPermissionsController,
    private val notificationPreferencesHolder: NotificationPreferencesHolder
) {

    private var checkJob: Job? = null

    fun checkUpdate() {
        if (!notificationPreferencesHolder.updateEnabled.get()) {
            return
        }
        cancel()
        checkJob = GlobalScope.launch(Dispatchers.Main) {
            coRunCatching {
                checkerRepository.checkUpdate(true)
            }.onSuccess {
                showUpdateData(it)
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun cancel() {
        checkJob?.cancel()
    }

    @SuppressLint("MissingPermission")
    private suspend fun showUpdateData(update: UpdateData) {
        val currentVersionCode = AppBuildConfig.versionCode

        if (update.code <= currentVersionCode) {
            return
        }
        if (!permissionsController.get(Manifest.permission.POST_NOTIFICATIONS).isGranted()) {
            return
        }
        val channelId = "forpda_channel_updates"
        val channelName = context.getString(R.string.updater_notification_title)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val mBuilder = NotificationCompat.Builder(context, channelId)

        val mNotificationManager = NotificationManagerCompat.from(context)

        mBuilder.setSmallIcon(R.drawable.ic_notify_mention)

        mBuilder.setContentTitle(context.getString(R.string.updater_notification_title))
        mBuilder.setContentText(context.getString(R.string.updater_notification_content_VerName, update.name))

        mBuilder.setChannelId(channelId)


        val notifyIntent = UpdateCheckerActivity.newIntent(context).apply {
            action = Intent.ACTION_VIEW
        }
        val notifyPendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, immutableFlag())
        mBuilder.setContentIntent(notifyPendingIntent)

        mBuilder.setAutoCancel(true)

        mBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        mBuilder.setCategory(NotificationCompat.CATEGORY_EVENT)

        var defaults = 0
        //defaults = defaults or NotificationCompat.DEFAULT_SOUND
        defaults = defaults or NotificationCompat.DEFAULT_VIBRATE
        mBuilder.setDefaults(defaults)

        mNotificationManager.notify(update.code, mBuilder.build())
    }
}
