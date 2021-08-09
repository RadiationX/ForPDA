package forpdateam.ru.forpda.notifications;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.common.BitmapUtils;
import forpdateam.ru.forpda.entity.remote.events.NotificationEvent;
import forpdateam.ru.forpda.model.SchedulersProvider;
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils;
import forpdateam.ru.forpda.model.repository.avatar.AvatarRepository;
import forpdateam.ru.forpda.model.repository.events.EventsRepository;
import forpdateam.ru.forpda.model.preferences.NotificationPreferencesHolder;
import forpdateam.ru.forpda.ui.activities.MainActivity;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

/**
 * Created by radiationx on 31.07.17.
 */

public class NotificationsService extends Service {
    private final static String LOG_TAG = NotificationsService.class.getSimpleName();
    private final static String CHANNEL_DEFAULT_ID = "forpda_channel_default";
    private final static String CHANNEL_DEFAULT_NAME = "forpda_channel_default";
    private final static String CHANNEL_FAV_ID = "forpda_channel_fav";
    private final static String CHANNEL_QMS_ID = "forpda_channel_qms";
    private final static String CHANNEL_MENTION_ID = "forpda_channel_mention";
    private final static String CHANNEL_SITE_ID = "forpda_channel_site";
    /*private final static String CHANNEL_FAV_NAME = "forpda_channel_fav";
    private final static String CHANNEL_QMS_NAME = "forpda_channel_qms";
    private final static String CHANNEL_MENTION_NAME = "forpda_channel_mention";
    private final static String CHANNEL_SITE_NAME = "forpda_channel_site";*/
    public final static String CHECK_LAST_EVENTS = "CHECK_LAST_EVENTS";
    private final static int NOTIFY_STACKED_QMS_ID = -123;
    private final static int NOTIFY_STACKED_FAV_ID = -234;
    private final static int STACKED_MAX = 4;
    private final Messenger myMessenger = new Messenger(new IncomingHandler());
    private NotificationManagerCompat mNotificationManager;
    private long lastHardCheckTime = 0;

    private AvatarRepository avatarRepository = App.get().Di().getAvatarRepository();
    private EventsRepository eventsRepository = App.get().Di().getEventsRepository();
    private NotificationPreferencesHolder notificationPreferencesHolder = App.get().Di().getNotificationPreferencesHolder();

    protected CompositeDisposable disposables = new CompositeDisposable();

    private void addToDisposable(Disposable disposable) {
        disposables.add(disposable);
    }

    public static void startAndCheck() {
        try {
            Intent intent = new Intent(App.getContext(), NotificationsService.class).setAction(NotificationsService.CHECK_LAST_EVENTS);
            App.getContext().startService(intent);
            App.getContext().bindService(intent, App.get().getServiceConnection(), Context.BIND_AUTO_CREATE);
        } catch (Exception ignore) {
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "onBind");
        return myMessenger.getBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "onUnbind");
        return true;
    }

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "onCreate");
        addToDisposable(
                notificationPreferencesHolder
                        .observeFavEnabled()
                        .subscribe(enabled -> {
                            if (enabled) {
                                eventsRepository.updateEvents(NotificationEvent.Source.THEME);
                            }
                        })
        );

        addToDisposable(
                notificationPreferencesHolder
                        .observeQmsEnabled()
                        .subscribe(enabled -> {
                            if (enabled) {
                                eventsRepository.updateEvents(NotificationEvent.Source.QMS);
                            }
                        })
        );

        addToDisposable(
                notificationPreferencesHolder
                        .observeMainLimit()
                        .subscribe(limit -> {
                            Log.d(LOG_TAG, "NEW timer period " + limit);
                            eventsRepository.setTimerPeriod(limit);
                        })
        );

        addToDisposable(
                eventsRepository
                        .observeEvents()
                        .subscribe(this::sendNotification)
        );

        addToDisposable(
                eventsRepository
                        .observeEventsStack()
                        .subscribe(this::sendNotifications)
        );

        addToDisposable(
                eventsRepository
                        .observeCancel()
                        .subscribe(this::cancelNotification)
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "onStartCommand this" + this + " : " + App.get());
        Log.i(LOG_TAG, "onStartCommand args" + flags + " : " + startId + " : " + intent);
        boolean checkEvents = intent != null && intent.getAction() != null && intent.getAction().equals(CHECK_LAST_EVENTS);
        long time = System.currentTimeMillis();

        Log.d(LOG_TAG, "Handle check last events: " + time + " : " + lastHardCheckTime + " : " + (time - lastHardCheckTime));

        if (checkEvents && ((time - lastHardCheckTime) >= 1000 * 60)) {
            lastHardCheckTime = time;
            checkEvents = true;
        } else {
            checkEvents = false;
        }
        eventsRepository.externalStart(checkEvents);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
        if (!disposables.isDisposed())
            disposables.dispose();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(LOG_TAG, "onTaskRemoved");
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Intent restartIntent = new Intent(this, getClass());

            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (am != null) {
                PendingIntent pi = PendingIntent.getService(this, 1, restartIntent, PendingIntent.FLAG_ONE_SHOT);
                restartIntent.putExtra("RESTART", "RESTART_CHEBUREK");
                am.setExact(AlarmManager.RTC, System.currentTimeMillis() + 3000, pi);
            }
        }
    }

    private NotificationManagerCompat getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = NotificationManagerCompat.from(this);
        }
        return mNotificationManager;
    }

    private void cancelNotification(NotificationEvent event) {
        Log.e("kulolo", "cancelNotification " + event.notifyId());
        getNotificationManager().cancel(event.notifyId());
    }

    public void sendNotification(NotificationEvent event) {
        Log.e("kulolo", "sendNotification " + event.notifyId());
        if (notificationPreferencesHolder.getMainAvatarsEnabled()) {
            SchedulersProvider schedulers = App.get().Di().getSchedulers();
            Disposable disposable = avatarRepository
                    .getAvatar(event.getUserId(), event.getUserNick())
                    .flatMap((Function<String, SingleSource<Bitmap>>) s -> {
                        return Single
                                .fromCallable(() -> ImageLoader.getInstance().loadImageSync(s))
                                .subscribeOn(schedulers.io())
                                .observeOn(schedulers.ui());
                    })
                    .onErrorReturn(throwable -> ImageLoader.getInstance().loadImageSync("assets://av.png"))
                    .map(bitmap -> {
                        if (bitmap != null) {
                            Resources res = App.getContext().getResources();
                            int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
                            int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
                            boolean isCircle = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

                            bitmap = BitmapUtils.centerCrop(bitmap, width, height, 1.0f);
                            bitmap = BitmapUtils.createAvatar(bitmap, width, height, isCircle);
                        }
                        return bitmap;
                    })
                    .subscribe(avatar -> sendNotification(event, avatar), Throwable::printStackTrace);
            addToDisposable(disposable);
        } else {
            sendNotification(event, null);
        }
    }

    public void sendNotification(NotificationEvent event, Bitmap avatar) {
        Log.e("events_lalala", "send notification " + event.getSourceEventText() + " : " + event.getSource() + " : " + event.getSourceTitle() + " : " + event.getUserNick());
        String title = createTitle(event);
        String text = createContent(event);
        String summaryText = createSummary(event);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(text);
        bigTextStyle.setSummaryText(summaryText);

        String channelId = getChannelId(event);
        String channelName = getChannelName(event);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);

        if (avatar != null && !event.fromSite()) {
            builder.setLargeIcon(avatar);
        }
        builder.setSmallIcon(createSmallIcon(event));

        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setStyle(bigTextStyle);
        builder.setChannelId(channelId);


        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setData(Uri.parse(createIntentUrl(event)));
        notifyIntent.setAction(Intent.ACTION_VIEW);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
        builder.setContentIntent(notifyPendingIntent);

        configureNotification(builder);

        getNotificationManager().cancel(event.notifyId());
        getNotificationManager().notify(event.notifyId(), builder.build());
    }

    public void sendNotifications(List<NotificationEvent> events) {
        String title = createStackedTitle(events);
        CharSequence text = createStackedContent(events);
        String summaryText = createStackedSummary(events);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(text);
        bigTextStyle.setSummaryText(summaryText);

        String channelId = getChannelId(events.get(0));
        String channelName = getChannelName(events.get(0));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);

        builder.setSmallIcon(createStackedSmallIcon(events));

        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setStyle(bigTextStyle);
        builder.setChannelId(channelId);

        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setData(Uri.parse(createStackedIntentUrl(events)));
        notifyIntent.setAction(Intent.ACTION_VIEW);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
        builder.setContentIntent(notifyPendingIntent);

        configureNotification(builder);

        int id = 0;
        NotificationEvent event = events.get(0);
        if (event.fromQms()) {
            id = NOTIFY_STACKED_QMS_ID;
        } else if (event.fromTheme()) {
            id = NOTIFY_STACKED_FAV_ID;
        }
        getNotificationManager().notify(id, builder.build());
    }

    private void configureNotification(NotificationCompat.Builder builder) {
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setCategory(NotificationCompat.CATEGORY_SOCIAL);
        int defaults = 0;
        if (notificationPreferencesHolder.getMainSoundEnabled()) {
            defaults |= NotificationCompat.DEFAULT_SOUND;
        }
        if (notificationPreferencesHolder.getMainVibrationEnabled()) {
            defaults |= NotificationCompat.DEFAULT_VIBRATE;
        }
        if (notificationPreferencesHolder.getMainIndicatorEnabled()) {
            defaults |= NotificationCompat.DEFAULT_LIGHTS;
        }
        builder.setDefaults(defaults);
        builder.setVibrate(new long[]{0L});
    }

    private String getChannelId(NotificationEvent event) {
        if (event.isMention())
            return CHANNEL_MENTION_ID;

        if (event.fromQms())
            return CHANNEL_QMS_ID;

        if (event.fromTheme())
            return CHANNEL_FAV_ID;

        if (event.fromSite())
            return CHANNEL_SITE_ID;

        return CHANNEL_DEFAULT_ID;
    }

    private String getChannelName(NotificationEvent event) {
        if (event.isMention())
            return getString(R.string.notification_summary_mention);

        if (event.fromQms())
            return getString(R.string.notification_summary_qms);

        if (event.fromTheme())
            return getString(R.string.notification_summary_fav);

        if (event.fromSite())
            return getString(R.string.notification_summary_comment);

        return CHANNEL_DEFAULT_NAME;
    }

    /*
     * DEFAULT EVENT
     * */

    @DrawableRes
    public int createSmallIcon(NotificationEvent event) {
        if (event.fromQms())
            return R.drawable.ic_notify_qms;

        if (event.fromTheme()) {
            if (event.isMention())
                return R.drawable.ic_notify_mention;

            return R.drawable.ic_notify_favorites;
        }

        if (event.fromSite())
            return R.drawable.ic_notify_site;

        return R.drawable.ic_notify_qms;
    }

    public String createTitle(NotificationEvent event) {
        if (event.fromQms()) {
            String nick = event.getUserNick();
            if (nick == null || nick.isEmpty())
                return "Сообщения 4PDA";
        }

        if (event.fromSite())
            return "ForPDA";

        return event.getUserNick();
    }

    public String createContent(NotificationEvent event) {
        if (event.fromQms())
            return String.format(getString(R.string.notification_content_qms_Nick_Count), event.getSourceTitle(), event.getMsgCount());

        if (event.fromTheme()) {
            if (event.isMention())
                return String.format(getString(R.string.notification_content_mention_Title), event.getSourceTitle());

            return String.format(getString(R.string.notification_content_theme_Title), event.getSourceTitle());
        }

        if (event.fromSite())
            return getString(R.string.notification_content_news);

        return "";
    }

    public String createSummary(NotificationEvent event) {
        if (event.isMention())
            return getString(R.string.notification_summary_mention);

        if (event.fromQms())
            return getString(R.string.notification_summary_qms);

        if (event.fromTheme())
            return getString(R.string.notification_summary_fav);

        if (event.fromSite())
            return getString(R.string.notification_summary_comment);

        return "";
    }

    public String createIntentUrl(NotificationEvent event) {
        if (event.isMention()) {
            if (event.fromTheme())
                return "https://4pda.to/forum/index.php?showtopic=" + event.getSourceId() + "&view=findpost&p=" + event.getMessageId();

            if (event.fromSite())
                return "https://4pda.to/index.php?p=" + event.getSourceId() + "/#comment" + event.getMessageId();
        }

        if (event.fromQms())
            return "https://4pda.to/forum/index.php?act=qms&mid=" + event.getUserId() + "&t=" + event.getSourceId();

        if (event.fromTheme())
            return "https://4pda.to/forum/index.php?showtopic=" + event.getSourceId() + "&view=getnewpost";

        return "";
    }


    /*
     * STACKED EVENTS
     * */
    private String createStackedTitle(List<NotificationEvent> events) {
        return createStackedSummary(events);
    }

    private CharSequence createStackedContent(List<NotificationEvent> events) {
        StringBuilder content = new StringBuilder();

        int size = Math.min(events.size(), STACKED_MAX);
        for (int i = 0; i < size; i++) {
            NotificationEvent event = events.get(i);
            if (event.fromQms()) {
                String nick = event.getUserNick();
                if (nick == null || nick.isEmpty())
                    nick = "Сообщения 4PDA";
                content.append("<b>").append(nick).append("</b>");
                content.append(": ").append(event.getSourceTitle());
            } else if (event.fromTheme()) {
                content.append(event.getSourceTitle());
            }
            if (i < size - 1) {
                content.append("<br>");
            }
        }

        if (events.size() > size) {
            content.append("<br>");
            content.append("...и еще ").append(events.size() - size);
        }

        return ApiUtils.spannedFromHtml(content.toString());
    }

    private String createStackedSummary(List<NotificationEvent> events) {
        return createSummary(events.get(0));
    }

    @DrawableRes
    public int createStackedSmallIcon(List<NotificationEvent> events) {
        return createSmallIcon(events.get(0));
    }

    private String createStackedIntentUrl(List<NotificationEvent> events) {
        NotificationEvent event = events.get(0);
        if (event.fromQms())
            return "https://4pda.to/forum/index.php?act=qms";

        if (event.fromTheme())
            return "https://4pda.to/forum/index.php?act=fav";

        return "";
    }

    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), "" + msg.getData(), Toast.LENGTH_SHORT).show();
        }
    }
}
