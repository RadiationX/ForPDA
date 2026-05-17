package forpdateam.ru.forpda.model.interactors.events

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.BitmapUtils.centerCrop
import forpdateam.ru.forpda.common.BitmapUtils.createAvatar
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.extensions.asMutableFlag
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.extensions.getDimenPx
import forpdateam.ru.forpda.model.interactors.events.models.NotificationEvent
import forpdateam.ru.forpda.model.interactors.events.models.NotificationId
import forpdateam.ru.forpda.model.preferences.NotificationPreferencesHolder
import forpdateam.ru.forpda.model.repository.avatar.AvatarRepository
import forpdateam.ru.forpda.ui.activities.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.mintrocket.lib.mintpermissions.MintPermissionsController
import ru.mintrocket.lib.mintpermissions.ext.isGranted

class NotificationEventSender(
    private val context: Context,
    private val preferences: NotificationPreferencesHolder,
    private val avatarRepository: AvatarRepository,
    private val permissionsController: MintPermissionsController
) {

    companion object {
        private val LOG_TAG = NotificationEventSender::class.java.simpleName

        private val DEPRECATED_CHANNEL_IDS = listOf(
            "forpda_channel_default",
            "forpda_channel_fav",
            "forpda_channel_forum",
            "forpda_channel_qms",
            "forpda_channel_mention",
            "forpda_channel_site",
        )
        private const val CHANNEL_FAV_ID = "forpda_channel_fav_v2"
        private const val CHANNEL_FORUM_ID = "forpda_channel_forum_v2"
        private const val CHANNEL_QMS_ID = "forpda_channel_qms_v2"
        private const val CHANNEL_MENTION_ID = "forpda_channel_mention_v2"
        private const val CHANNEL_SITE_ID = "forpda_channel_site_v2"
    }

    suspend fun send(event: NotificationEvent) {
        if (!preferences.mainEnabled.get()) {
            return
        }

        if (!event.checkEnabled()) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (permissionsController.get(Manifest.permission.POST_NOTIFICATIONS).isGranted()) {
                sendNotification(context, event.toParams(context))
            }
        } else {
            sendNotification(context, event.toParams(context))
        }
    }

    fun cancel(id: NotificationId) {
        NotificationManagerCompat
            .from(context)
            .cancel(id::class.qualifiedName, id.hashCode())
    }

    @SuppressLint("MissingPermission")
    private fun sendNotification(context: Context, params: NotificationParams) {
        val manager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DEPRECATED_CHANNEL_IDS.forEach {
                manager.deleteNotificationChannel(it)
            }

            val channel = NotificationChannelCompat.Builder(params.channelId, NotificationManager.IMPORTANCE_DEFAULT)
                .setName(params.channelName)
                .setVibrationEnabled(true)
                .setLightsEnabled(true)
                .build()

            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, params.channelId)

        builder.setLargeIcon(params.largeIcon)
        builder.setSmallIcon(params.smallIconRes)

        builder.setContentTitle(params.contentTitle)
        builder.setContentText(params.contentText)

        val bigTextStyle = NotificationCompat.BigTextStyle().also {
            it.setBigContentTitle(params.contentTitle)
            it.bigText(params.contentText)
            it.setSummaryText(params.summary)
        }
        builder.setStyle(bigTextStyle)


        val notifyIntent = Intent(context, MainActivity::class.java)
        notifyIntent.setData(params.intentUrl.toUri())
        notifyIntent.setAction(Intent.ACTION_VIEW)
        val notifyPendingIntent = PendingIntent.getActivity(
            context,
            0,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT.asMutableFlag()
        )
        builder.setContentIntent(notifyPendingIntent)

        configureNotification(builder)

        cancel(params.id)
        manager.notify(params.id::class.qualifiedName, params.id.hashCode(), builder.build())
    }

    // TODO defaults is deprecated android 8+
    private fun configureNotification(builder: NotificationCompat.Builder) {
        builder.setAutoCancel(true)
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        builder.setCategory(NotificationCompat.CATEGORY_SOCIAL)
        var defaults = 0
        if (preferences.mainSoundEnabled.get()) {
            defaults = defaults or NotificationCompat.DEFAULT_SOUND
        }
        if (preferences.mainVibrationEnabled.get()) {
            defaults = defaults or NotificationCompat.DEFAULT_VIBRATE
        }
        if (preferences.mainIndicatorEnabled.get()) {
            defaults = defaults or NotificationCompat.DEFAULT_ALL
        }
        builder.setDefaults(defaults)
    }

    private suspend fun NotificationEvent.toParams(context: Context): NotificationParams {
        return NotificationParams(
            id = toId(),
            channelId = toChannelId(),
            channelName = toChannelName(context),
            contentText = toContentText(context),
            contentTitle = toContentTitle(),
            summary = toSummary(context),
            smallIconRes = toSmallIcon(),
            largeIcon = toLargeIcon(),
            intentUrl = toIntentUrl(),
            timeStamp = toTimestamp()
        )
    }


    private fun NotificationEvent.checkEnabled(): Boolean = when (this) {
        is NotificationEvent.Favorite -> {
            if (preferences.favOnlyImportant.get()) {
                data.isImportant
            } else {
                preferences.favEnabled.get()
            }
        }

        is NotificationEvent.Forum -> preferences.forumsEnabled.get()
        is NotificationEvent.Qms -> preferences.qmsEnabled.get()
        is NotificationEvent.SiteMention -> preferences.siteMentionsEnabled.get()
        is NotificationEvent.TopicMention -> preferences.topicMentionsEnabled.get()
    }

    private fun NotificationEvent.toChannelId(): String = when (this) {
        is NotificationEvent.Favorite -> CHANNEL_FAV_ID
        is NotificationEvent.Forum -> CHANNEL_FORUM_ID
        is NotificationEvent.Qms -> CHANNEL_QMS_ID
        is NotificationEvent.SiteMention -> CHANNEL_SITE_ID
        is NotificationEvent.TopicMention -> CHANNEL_MENTION_ID
    }

    private fun NotificationEvent.toChannelName(context: Context): String = when (this) {
        is NotificationEvent.Favorite -> context.getString(R.string.notification_summary_fav)
        is NotificationEvent.Forum -> context.getString(R.string.notification_summary_forum)
        is NotificationEvent.Qms -> context.getString(R.string.notification_summary_qms)
        is NotificationEvent.SiteMention -> context.getString(R.string.notification_summary_comment)
        is NotificationEvent.TopicMention -> context.getString(R.string.notification_summary_mention)
    }

    private fun NotificationEvent.toContentText(context: Context): String = when (this) {
        is NotificationEvent.Favorite -> {
            context.getString(
                R.string.notification_content_theme_Title,
                data.sourceTitle
            )

        }

        is NotificationEvent.Forum -> {
            context.getString(R.string.notification_content_forum)
        }

        is NotificationEvent.Qms -> {
            context.getString(
                R.string.notification_content_qms_Nick_Count,
                data.sourceTitle,
                data.msgCount
            )
        }

        is NotificationEvent.SiteMention -> {
            context.getString(R.string.notification_content_news)
        }

        is NotificationEvent.TopicMention -> {
            context.getString(R.string.notification_content_topic_mention)
        }
    }

    private fun NotificationEvent.toContentTitle(): String = when (this) {
        is NotificationEvent.Favorite -> data.user.nick
        is NotificationEvent.Forum -> "ForPDA"
        is NotificationEvent.Qms -> data.user.nick
        is NotificationEvent.SiteMention -> "ForPDA"
        is NotificationEvent.TopicMention -> "ForPDA"
    }

    private fun NotificationEvent.toSummary(context: Context): String = when (this) {
        is NotificationEvent.Favorite -> context.getString(R.string.notification_summary_fav)
        is NotificationEvent.Forum -> context.getString(R.string.notification_summary_forum)
        is NotificationEvent.Qms -> context.getString(R.string.notification_summary_qms)
        is NotificationEvent.SiteMention -> context.getString(R.string.notification_summary_comment)
        is NotificationEvent.TopicMention -> context.getString(R.string.notification_summary_mention)
    }

    @DrawableRes
    private fun NotificationEvent.toSmallIcon(): Int = when (this) {
        is NotificationEvent.Favorite -> R.drawable.ic_notify_favorites
        is NotificationEvent.Forum -> R.drawable.ic_notify_qms
        is NotificationEvent.Qms -> R.drawable.ic_notify_qms
        is NotificationEvent.SiteMention -> R.drawable.ic_notify_site
        is NotificationEvent.TopicMention -> R.drawable.ic_notify_mention
    }

    private suspend fun NotificationEvent.toLargeIcon(): Bitmap? = when (this) {
        is NotificationEvent.Favorite -> createAvatarBitmap(data.user)
        is NotificationEvent.Forum -> null
        is NotificationEvent.Qms -> createAvatarBitmap(data.user)
        is NotificationEvent.SiteMention -> null
        is NotificationEvent.TopicMention -> null
    }

    private fun NotificationEvent.toIntentUrl(): String = when (this) {
        is NotificationEvent.Favorite -> "https://4pda.to/forum/index.php?showtopic=${id.topicId}&view=getnewpost"
        is NotificationEvent.Forum -> "https://4pda.to/forum/index.php?showforum=${id.forumId}"
        is NotificationEvent.Qms -> "https://4pda.to/forum/index.php?act=qms&mid=${data.user.id}&t=${id.themeId}"
        is NotificationEvent.SiteMention -> "https://4pda.to/index.php?p=${id.articleId}/#comment$commentId"
        is NotificationEvent.TopicMention -> "https://4pda.to/forum/index.php?showtopic=${id.topicId}&view=findpost&p=$postId"
    }

    private fun NotificationEvent.toTimestamp(): Long? = when (this) {
        is NotificationEvent.Favorite -> data.timeStamp
        is NotificationEvent.Forum -> null
        is NotificationEvent.Qms -> data.timeStamp
        is NotificationEvent.SiteMention -> null
        is NotificationEvent.TopicMention -> null
    }

    private fun NotificationEvent.toId(): NotificationId = when (this) {
        is NotificationEvent.Favorite -> id
        is NotificationEvent.Forum -> id
        is NotificationEvent.Qms -> id
        is NotificationEvent.SiteMention -> id
        is NotificationEvent.TopicMention -> id
    }

    private suspend fun createAvatarBitmap(user: User): Bitmap? {
        val avatarUrl = avatarRepository.getAvatar(user.id, user.nick)
        val avatar = withContext(Dispatchers.IO) {
            coRunCatching {
                ImageLoader.getInstance().loadImageSync(avatarUrl)
            }.recoverCatching {
                ImageLoader.getInstance().loadImageSync("assets://av.png")
            }
        }.mapCatching { bitmap ->
            withContext(Dispatchers.Default) {
                val height = context.getDimenPx(android.R.dimen.notification_large_icon_height)
                val width = context.getDimenPx(android.R.dimen.notification_large_icon_width)

                centerCrop(bitmap, width, height, 1.0f).let {
                    createAvatar(it, width, height, true)
                }
            }
        }.onFailure {
            Log.d(LOG_TAG, "get avatar", it)
        }
        return avatar.getOrNull()
    }

    private data class NotificationParams(
        val id: NotificationId,
        val channelId: String,
        val channelName: String,
        val contentText: String,
        val contentTitle: String,
        val summary: String,
        @param:DrawableRes val smallIconRes: Int,
        val largeIcon: Bitmap?,
        val intentUrl: String,
        val timeStamp: Long?
    )
}