package forpdateam.ru.forpda.notifications

import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.App.Companion.get
import forpdateam.ru.forpda.App.Companion.getContext
import forpdateam.ru.forpda.common.BitmapUtils.centerCrop
import forpdateam.ru.forpda.common.BitmapUtils.createAvatar
import forpdateam.ru.forpda.entity.remote.events.NotificationEvent
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils.spannedFromHtml
import forpdateam.ru.forpda.ui.activities.MainActivity
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import kotlin.math.min

/**
 * Created by radiationx on 31.07.17.
 */
class NotificationsService : Service() {
    private val myMessenger = Messenger(IncomingHandler())
    private var lastHardCheckTime: Long = 0

    private val avatarRepository = get().Di().avatarRepository
    private val eventsRepository = get().Di().eventsRepository
    private val notificationPreferencesHolder = get().Di().notificationPreferencesHolder

    protected var disposables: CompositeDisposable = CompositeDisposable()

    private fun addToDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.v(LOG_TAG, "onBind")
        return myMessenger.binder
    }

    override fun onRebind(intent: Intent) {
        Log.v(LOG_TAG, "onRebind")
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.v(LOG_TAG, "onUnbind")
        return true
    }

    override fun onCreate() {
        Log.i(LOG_TAG, "onCreate")
        addToDisposable(
            notificationPreferencesHolder
                .observeFavEnabled()
                .subscribe { enabled: Boolean ->
                    if (enabled) {
                        eventsRepository.updateEvents(NotificationEvent.Source.THEME)
                    }
                }
        )

        addToDisposable(
            notificationPreferencesHolder
                .observeQmsEnabled()
                .subscribe { enabled: Boolean ->
                    if (enabled) {
                        eventsRepository.updateEvents(NotificationEvent.Source.QMS)
                    }
                }
        )

        addToDisposable(
            notificationPreferencesHolder
                .observeMainLimit()
                .subscribe { limit: Long ->
                    Log.d(
                        LOG_TAG,
                        "NEW timer period $limit"
                    )
                    eventsRepository.setTimerPeriod(limit)
                }
        )

        addToDisposable(
            eventsRepository
                .observeEvents()
                .subscribe { event: NotificationEvent -> this.sendNotification(event) }
        )

        addToDisposable(
            eventsRepository
                .observeEventsStack()
                .subscribe { events: List<NotificationEvent> ->
                    this.sendNotifications(
                        events
                    )
                }
        )

        addToDisposable(
            eventsRepository
                .observeCancel()
                .subscribe { event: NotificationEvent -> this.cancelNotification(event) }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(LOG_TAG, "onStartCommand this" + this + " : " + get())
        Log.i(
            LOG_TAG,
            "onStartCommand args$flags : $startId : $intent"
        )
        var checkEvents =
            intent != null && intent.action != null && intent.action == CHECK_LAST_EVENTS
        val time = System.currentTimeMillis()

        Log.d(
            LOG_TAG,
            "Handle check last events: " + time + " : " + lastHardCheckTime + " : " + (time - lastHardCheckTime)
        )

        if (checkEvents && ((time - lastHardCheckTime) >= 1000 * 60)) {
            lastHardCheckTime = time
            checkEvents = true
        } else {
            checkEvents = false
        }
        eventsRepository.externalStart(checkEvents)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(LOG_TAG, "onDestroy")
        if (!disposables.isDisposed) disposables.dispose()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        Log.i(LOG_TAG, "onTaskRemoved")
    }

    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(this)
    }

    private fun cancelNotification(event: NotificationEvent) {
        Log.e("kulolo", "cancelNotification " + event.notifyId())
        notificationManager.cancel(event.notifyId())
    }

    fun sendNotification(event: NotificationEvent) {
        Log.e("kulolo", "sendNotification " + event.notifyId())
        if (notificationPreferencesHolder.getMainAvatarsEnabled()) {
            val schedulers = get().Di().schedulers
            val disposable = avatarRepository
                .getAvatar(event.userId, event.userNick)
                .flatMap(Function<String?, SingleSource<Bitmap>> { s: String? ->
                    Single
                        .fromCallable { ImageLoader.getInstance().loadImageSync(s) }
                        .subscribeOn(schedulers.io())
                        .observeOn(schedulers.ui())
                })
                .onErrorReturn { throwable: Throwable? ->
                    ImageLoader.getInstance().loadImageSync("assets://av.png")
                }
                .map { bitmap: Bitmap? ->
                    var bitmap = bitmap
                    if (bitmap != null) {
                        val res = getContext().resources
                        val height =
                            res.getDimension(R.dimen.notification_large_icon_height).toInt()
                        val width =
                            res.getDimension(R.dimen.notification_large_icon_width).toInt()

                        bitmap = centerCrop(bitmap, width, height, 1.0f)
                        bitmap = createAvatar(bitmap, width, height, true)
                    }
                    bitmap
                }
                .subscribe(
                    { avatar: Bitmap? -> sendNotification(event, avatar) },
                    { obj: Throwable -> obj.printStackTrace() })
            addToDisposable(disposable)
        } else {
            sendNotification(event, null)
        }
    }

    fun sendNotification(event: NotificationEvent, avatar: Bitmap?) {
        Log.e(
            "events_lalala",
            "send notification " + event.sourceEventText + " : " + event.source + " : " + event.sourceTitle + " : " + event.userNick
        )
        val title = createTitle(event)
        val text = createContent(event)
        val summaryText = createSummary(event)

        val bigTextStyle = NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle(title)
        bigTextStyle.bigText(text)
        bigTextStyle.setSummaryText(summaryText)

        val channelId = getChannelId(event)
        val channelName = getChannelName(event)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager?.createNotificationChannel(channel)
        }


        val builder = NotificationCompat.Builder(this, channelId)

        if (avatar != null && !event.fromSite()) {
            builder.setLargeIcon(avatar)
        }
        builder.setSmallIcon(createSmallIcon(event))

        builder.setContentTitle(title)
        builder.setContentText(text)
        builder.setStyle(bigTextStyle)
        builder.setChannelId(channelId)


        val notifyIntent = Intent(this, MainActivity::class.java)
        notifyIntent.setData(Uri.parse(createIntentUrl(event)))
        notifyIntent.setAction(Intent.ACTION_VIEW)
        val notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0)
        builder.setContentIntent(notifyPendingIntent)

        configureNotification(builder)

        notificationManager.cancel(event.notifyId())
        notificationManager.notify(event.notifyId(), builder.build())
    }

    fun sendNotifications(events: List<NotificationEvent>) {
        val title = createStackedTitle(events)
        val text = createStackedContent(events)
        val summaryText = createStackedSummary(events)

        val bigTextStyle = NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle(title)
        bigTextStyle.bigText(text)
        bigTextStyle.setSummaryText(summaryText)

        val channelId = getChannelId(events[0])
        val channelName = getChannelName(events[0])

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager?.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)

        builder.setSmallIcon(createStackedSmallIcon(events))

        builder.setContentTitle(title)
        builder.setContentText(text)
        builder.setStyle(bigTextStyle)
        builder.setChannelId(channelId)

        val notifyIntent = Intent(this, MainActivity::class.java)
        notifyIntent.setData(Uri.parse(createStackedIntentUrl(events)))
        notifyIntent.setAction(Intent.ACTION_VIEW)
        val notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0)
        builder.setContentIntent(notifyPendingIntent)

        configureNotification(builder)

        var id = 0
        val event = events[0]
        if (event.fromQms()) {
            id = NOTIFY_STACKED_QMS_ID
        } else if (event.fromTheme()) {
            id = NOTIFY_STACKED_FAV_ID
        }
        notificationManager.notify(id, builder.build())
    }

    private fun configureNotification(builder: NotificationCompat.Builder) {
        builder.setAutoCancel(true)
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        builder.setCategory(NotificationCompat.CATEGORY_SOCIAL)
        var defaults = 0
        if (notificationPreferencesHolder.getMainSoundEnabled()) {
            defaults = defaults or NotificationCompat.DEFAULT_SOUND
        }
        if (notificationPreferencesHolder.getMainVibrationEnabled()) {
            defaults = defaults or NotificationCompat.DEFAULT_VIBRATE
        }
        if (notificationPreferencesHolder.getMainIndicatorEnabled()) {
            defaults = defaults or NotificationCompat.DEFAULT_LIGHTS
        }
        builder.setDefaults(defaults)
        builder.setVibrate(longArrayOf(0L))
    }

    private fun getChannelId(event: NotificationEvent): String {
        if (event.isMention) return CHANNEL_MENTION_ID

        if (event.fromQms()) return CHANNEL_QMS_ID

        if (event.fromTheme()) return CHANNEL_FAV_ID

        if (event.fromSite()) return CHANNEL_SITE_ID

        return CHANNEL_DEFAULT_ID
    }

    private fun getChannelName(event: NotificationEvent): String {
        if (event.isMention) return getString(forpdateam.ru.forpda.R.string.notification_summary_mention)

        if (event.fromQms()) return getString(forpdateam.ru.forpda.R.string.notification_summary_qms)

        if (event.fromTheme()) return getString(forpdateam.ru.forpda.R.string.notification_summary_fav)

        if (event.fromSite()) return getString(forpdateam.ru.forpda.R.string.notification_summary_comment)

        return CHANNEL_DEFAULT_NAME
    }

    /*
     * DEFAULT EVENT
     * */
    @DrawableRes
    fun createSmallIcon(event: NotificationEvent): Int {
        if (event.fromQms()) return forpdateam.ru.forpda.R.drawable.ic_notify_qms

        if (event.fromTheme()) {
            if (event.isMention) return forpdateam.ru.forpda.R.drawable.ic_notify_mention

            return forpdateam.ru.forpda.R.drawable.ic_notify_favorites
        }

        if (event.fromSite()) return forpdateam.ru.forpda.R.drawable.ic_notify_site

        return forpdateam.ru.forpda.R.drawable.ic_notify_qms
    }

    fun createTitle(event: NotificationEvent): String {
        if (event.fromQms()) {
            val nick = event.userNick
            if (nick == null || nick.isEmpty()) return "Сообщения 4PDA"
        }

        if (event.fromSite()) return "ForPDA"

        return event.userNick
    }

    fun createContent(event: NotificationEvent): String {
        if (event.fromQms()) return String.format(
            getString(forpdateam.ru.forpda.R.string.notification_content_qms_Nick_Count),
            event.sourceTitle,
            event.msgCount
        )

        if (event.fromTheme()) {
            if (event.isMention) return String.format(
                getString(forpdateam.ru.forpda.R.string.notification_content_mention_Title),
                event.sourceTitle
            )

            return String.format(
                getString(forpdateam.ru.forpda.R.string.notification_content_theme_Title),
                event.sourceTitle
            )
        }

        if (event.fromSite()) return getString(forpdateam.ru.forpda.R.string.notification_content_news)

        return ""
    }

    fun createSummary(event: NotificationEvent): String {
        if (event.isMention) return getString(forpdateam.ru.forpda.R.string.notification_summary_mention)

        if (event.fromQms()) return getString(forpdateam.ru.forpda.R.string.notification_summary_qms)

        if (event.fromTheme()) return getString(forpdateam.ru.forpda.R.string.notification_summary_fav)

        if (event.fromSite()) return getString(forpdateam.ru.forpda.R.string.notification_summary_comment)

        return ""
    }

    fun createIntentUrl(event: NotificationEvent): String {
        if (event.isMention) {
            if (event.fromTheme()) return "https://4pda.to/forum/index.php?showtopic=" + event.sourceId + "&view=findpost&p=" + event.messageId

            if (event.fromSite()) return "https://4pda.to/index.php?p=" + event.sourceId + "/#comment" + event.messageId
        }

        if (event.fromQms()) return "https://4pda.to/forum/index.php?act=qms&mid=" + event.userId + "&t=" + event.sourceId

        if (event.fromTheme()) return "https://4pda.to/forum/index.php?showtopic=" + event.sourceId + "&view=getnewpost"

        return ""
    }


    /*
     * STACKED EVENTS
     * */
    private fun createStackedTitle(events: List<NotificationEvent>): String {
        return createStackedSummary(events)
    }

    private fun createStackedContent(events: List<NotificationEvent>): CharSequence? {
        val content = StringBuilder()

        val size = min(
            events.size.toDouble(),
            STACKED_MAX.toDouble()
        ).toInt()
        for (i in 0 until size) {
            val event = events[i]
            if (event.fromQms()) {
                var nick = event.userNick
                if (nick == null || nick.isEmpty()) nick = "Сообщения 4PDA"
                content.append("<b>").append(nick).append("</b>")
                content.append(": ").append(event.sourceTitle)
            } else if (event.fromTheme()) {
                content.append(event.sourceTitle)
            }
            if (i < size - 1) {
                content.append("<br>")
            }
        }

        if (events.size > size) {
            content.append("<br>")
            content.append("...и еще ").append(events.size - size)
        }

        return spannedFromHtml(content.toString())
    }

    private fun createStackedSummary(events: List<NotificationEvent>): String {
        return createSummary(events[0])
    }

    @DrawableRes
    fun createStackedSmallIcon(events: List<NotificationEvent>): Int {
        return createSmallIcon(events[0])
    }

    private fun createStackedIntentUrl(events: List<NotificationEvent>): String {
        val event = events[0]
        if (event.fromQms()) return "https://4pda.to/forum/index.php?act=qms"

        if (event.fromTheme()) return "https://4pda.to/forum/index.php?act=fav"

        return ""
    }

    @SuppressLint("HandlerLeak")
    internal inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            Toast.makeText(applicationContext, "" + msg.data, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private val LOG_TAG = NotificationsService::class.java.simpleName
        private const val CHANNEL_DEFAULT_ID = "forpda_channel_default"
        private const val CHANNEL_DEFAULT_NAME = "forpda_channel_default"
        private const val CHANNEL_FAV_ID = "forpda_channel_fav"
        private const val CHANNEL_QMS_ID = "forpda_channel_qms"
        private const val CHANNEL_MENTION_ID = "forpda_channel_mention"
        private const val CHANNEL_SITE_ID = "forpda_channel_site"

        /*private final static String CHANNEL_FAV_NAME = "forpda_channel_fav";
    private final static String CHANNEL_QMS_NAME = "forpda_channel_qms";
    private final static String CHANNEL_MENTION_NAME = "forpda_channel_mention";
    private final static String CHANNEL_SITE_NAME = "forpda_channel_site";*/
        const val CHECK_LAST_EVENTS: String = "CHECK_LAST_EVENTS"
        private const val NOTIFY_STACKED_QMS_ID = -123
        private const val NOTIFY_STACKED_FAV_ID = -234
        private const val STACKED_MAX = 4
        fun startAndCheck() {
            try {
                val intent = Intent(getContext(), NotificationsService::class.java).setAction(
                    CHECK_LAST_EVENTS
                )
                getContext().startService(intent)
                getContext().bindService(intent, get().serviceConnection, BIND_AUTO_CREATE)
            } catch (ignore: Exception) {
            }
        }
    }
}
