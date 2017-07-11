package forpdateam.ru.forpda;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.events.Events;
import forpdateam.ru.forpda.api.events.models.NotificationEvent;
import forpdateam.ru.forpda.api.events.models.WebSocketEvent;
import forpdateam.ru.forpda.api.others.user.ForumUser;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.rxapi.ForumUsersCache;
import forpdateam.ru.forpda.utils.BitmapUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by isanechek on 7/11/17.
 */

// Yе стал копипастит последнию версию notification sevice, если все будет ок, тогда и чета надо будет думать. Чтобы не копипастить каждый раз

// for lollipop+
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NewWebSocketService extends JobService {
    private static final String TAG = "TestService";
    public final static String CHECK_LAST_EVENTS = "SOSNI_HUICA_DOZE";
    private NotificationManagerCompat mNotificationManager;
    private SparseArray<WebSocketEvent> notificationEvents = new SparseArray<>();
    private WebSocket webSocket;

    // Страшная хуйня для лоллипопа и выше.
    //
    public static int JOB_ID = 777;
    public static long SECONDS = 1000;
    public static long MINUTES = 60 * SECONDS;
    public static float INTERVAL_JITTER_FRAC = 0.25f;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.v(TAG, "Starting job: " + String.valueOf(params));
        webSocket = Client.getInstance().createWebSocketConnection(webSocketListener);
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            manager = getSystemService(NotificationManager.class);
        } else {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }*/
        webSocket.send("[0,\"sv\"]");
        webSocket.send("[0, \"ea\", \"u" + ClientHelper.getUserId() + "\"]");
        if (mNotificationManager == null) {
            mNotificationManager = NotificationManagerCompat.from(this);
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private static JobScheduler createScheduler(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return context.getSystemService(JobScheduler.class);
        }
        return (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    public static void registerJob(Context context, long intervalSeconds) {
        JobScheduler js = createScheduler(context);
        // а вдруг работает? а мы его хуяк и к отцам.
        js.cancel(JOB_ID);
        // здесь сам Бог андроида велит выпить, если вы не с нами на одной волне.
        long interval = intervalSeconds * SECONDS;
        long jitter = (long) (INTERVAL_JITTER_FRAC * interval);
        interval += (long) (Math.random() * (2 * jitter)) - jitter;
        final JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, NewWebSocketService.class))
                .setMinimumLatency(interval)
                .build();
        js.schedule(jobInfo);
        Log.v(TAG, "Time " + interval + "ms: " + String.valueOf(jobInfo));
    }


    public static void cancelJob(Context context) {
        JobScheduler js = createScheduler(context);
        Log.v(TAG, "Canceling job");
        js.cancel(JOB_ID);
    }

    private WebSocketListener webSocketListener = new WebSocketListener() {
        Matcher matcher = null;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.d("WS_EVENT", "ON OPEN: " + response.toString());
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d("WS_EVENT", "ON T MESSAGE: " + text);
            if (matcher == null) {
                matcher = Events.webSocketEventPattern.matcher(text);
            } else {
                matcher = matcher.reset(text);
            }
            WebSocketEvent event = Api.Events().parseWebSocketEvent(matcher);
            if (event != null) {
                handleWebSocketEvent(event);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            Log.d("WS_EVENT", "ON B MESSAGE: " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            Log.d("WS_EVENT", "ON CLOSING: " + code + " " + reason);
            webSocket.close(1000, null);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            Log.d("WS_EVENT", "ON CLOSED: " + code + " " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.d("WS_EVENT", "ON FAILURE: " + t.getMessage() + " " + response);
            t.printStackTrace();
            NewWebSocketService.this.webSocket.cancel();
            NewWebSocketService.this.webSocket = null;
        }
    };

    private void handleWebSocketEvent(WebSocketEvent webSocketEvent) {
        if (webSocketEvent.getEventCode() == WebSocketEvent.EVENT_READ) {
            WebSocketEvent oldWebSocketEvent = notificationEvents.get(webSocketEvent.createNotificationId(WebSocketEvent.EVENT_NEW));

            if (webSocketEvent.getType() == WebSocketEvent.TYPE_THEME) {
                //Убираем уведомления избранного
                if (oldWebSocketEvent != null && webSocketEvent.getMessageId() >= oldWebSocketEvent.getMessageId()) {
                    mNotificationManager.cancel(oldWebSocketEvent.createNotificationId());
                }

                //Убираем уведомление упоминаний
                oldWebSocketEvent = notificationEvents.get(webSocketEvent.createNotificationId(WebSocketEvent.EVENT_MENTION));
                if (oldWebSocketEvent != null) {
                    mNotificationManager.cancel(oldWebSocketEvent.createNotificationId());
                }
            } else if (webSocketEvent.getType() == WebSocketEvent.TYPE_QMS) {

                //Убираем уведомление кумыса
                if (oldWebSocketEvent != null) {
                    mNotificationManager.cancel(oldWebSocketEvent.createNotificationId());
                }
            }
        } else {
            notificationEvents.put(webSocketEvent.createNotificationId(), webSocketEvent);
            switch (webSocketEvent.getType()) {
                case WebSocketEvent.TYPE_QMS:
                    if (webSocketEvent.getEventCode() == WebSocketEvent.EVENT_NEW) {
                        handleQmsEvent(webSocketEvent);
                    }
                    break;
                case WebSocketEvent.TYPE_THEME:
                    if (webSocketEvent.getEventCode() == WebSocketEvent.EVENT_NEW || webSocketEvent.getEventCode() == WebSocketEvent.EVENT_MENTION) {
                        handleFavEvent(webSocketEvent);
                    }
                    break;
                case WebSocketEvent.TYPE_SITE:
                    if (webSocketEvent.getEventCode() == WebSocketEvent.EVENT_MENTION) {
                        handleSiteEvent(webSocketEvent);
                    }
                    break;
            }
        }
    }

    private void handleQmsEvent(WebSocketEvent webSocketEvent) {
        Observable.fromCallable(() -> Api.Events().getQmsEvents())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qmsEvents -> {
                    for (NotificationEvent notificationEvent : qmsEvents) {
                        if (notificationEvent.getThemeId() == webSocketEvent.getId()) {
                            notificationEvent.setWebSocketEvent(webSocketEvent);
                            sendNotification(notificationEvent);
                        }
                    }
                });
    }

    private void handleFavEvent(WebSocketEvent webSocketEvent) {
        Observable.fromCallable(() -> Api.Events().getFavoritesEvents())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(favEvents -> {
                    for (NotificationEvent notificationEvent : favEvents) {
                        if (notificationEvent.getThemeId() == webSocketEvent.getId()) {
                            notificationEvent.setWebSocketEvent(webSocketEvent);
                            sendNotification(notificationEvent);
                        }
                    }
                });
    }

    private void handleSiteEvent(WebSocketEvent webSocketEvent) {
        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setWebSocketEvent(webSocketEvent);
        notificationEvent.setThemeId(webSocketEvent.getId());
        sendNotification(notificationEvent);
    }

    public Bitmap loadAvatar(NotificationEvent notificationEvent) throws Exception {
        Bitmap bitmap = null;
        if (notificationEvent.getWebSocketEvent().getType() != WebSocketEvent.TYPE_SITE) {
            ForumUser forumUser = ForumUsersCache.getUserById(notificationEvent.getUserId());
            Log.d("WS_USER", "FORUM USER CACHE " + forumUser);
            if (forumUser == null) {
                forumUser = ForumUsersCache.loadUserByNick(notificationEvent.getUserNick());
                Log.d("WS_USER", "FORUM USER LOADED " + forumUser);
            }
            if (forumUser != null) {
                bitmap = ImageLoader.getInstance().loadImageSync(forumUser.getAvatar());
                Log.d("WS_BITMAP", "" + bitmap);
                if (bitmap != null) {
                    Log.d("WS_BITMAP", "" + bitmap.getHeight() + " : " + bitmap.getWidth());
                }
            }
        }
        if (bitmap != null) {
            Resources res = App.getContext().getResources();
            int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
            int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);

            Bitmap prevBitmap = bitmap;
            bitmap = BitmapUtils.centerCrop(bitmap, width, height, 1.0f);
            prevBitmap.recycle();

            boolean isCircle = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
            bitmap = BitmapUtils.createAvatar(bitmap, width, height, isCircle);
        }

        return bitmap;
    }

    public void sendNotification(NotificationEvent notificationEvent) {
        if (notificationEvent.getUserId() == ClientHelper.getUserId()) {
            return;
        }
        WebSocketEvent webSocketEvent = notificationEvent.getWebSocketEvent();


        String title = generateTitle(notificationEvent);
        String text = generateContentText(notificationEvent);
        String summaryText = generateSummaryText(notificationEvent);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(text);
        bigTextStyle.setSummaryText(summaryText);

        Observable.fromCallable(() -> loadAvatar(notificationEvent))
                .onErrorReturnItem(ImageLoader.getInstance().loadImageSync("assets://av.png"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    Log.d("WS_RX_BITMAP", "" + bitmap);

                    NotificationCompat.Builder mBuilder;
                    mBuilder = new NotificationCompat.Builder(this);

                    if (bitmap != null && webSocketEvent.getType() != WebSocketEvent.TYPE_SITE) {
                        mBuilder.setLargeIcon(bitmap);
                    }
                    mBuilder.setSmallIcon(generateSmallIcon(notificationEvent));

                    mBuilder.setContentTitle(title);
                    mBuilder.setContentText(text);
                    mBuilder.setStyle(bigTextStyle);


                    Intent notifyIntent = new Intent(this, MainActivity.class);
                    notifyIntent.setData(Uri.parse(generateIntentUrl(notificationEvent)));
                    notifyIntent.setAction(Intent.ACTION_VIEW);
                    PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
                    mBuilder.setContentIntent(notifyPendingIntent);

                    mBuilder.setAutoCancel(true);

                    mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    mBuilder.setCategory(NotificationCompat.CATEGORY_SOCIAL);
                    mBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);

                    mNotificationManager.cancel(webSocketEvent.createNotificationId());
                    mNotificationManager.notify(webSocketEvent.createNotificationId(), mBuilder.build());
                });
    }

    @DrawableRes
    public int generateSmallIcon(NotificationEvent notificationEvent) {
        WebSocketEvent webSocketEvent = notificationEvent.getWebSocketEvent();

        switch (webSocketEvent.getType()) {
            case WebSocketEvent.TYPE_QMS:
                return R.drawable.ic_notify_qms;
            case WebSocketEvent.TYPE_THEME:
                if (webSocketEvent.getEventCode() == WebSocketEvent.EVENT_MENTION) {
                    return R.drawable.ic_notify_mention;
                }
                return R.drawable.ic_notify_favorites;
            case WebSocketEvent.TYPE_SITE:
                return R.drawable.ic_notify_site;
        }
        return R.drawable.ic_notify_qms;
    }

    public String generateTitle(NotificationEvent notificationEvent) {
        WebSocketEvent webSocketEvent = notificationEvent.getWebSocketEvent();
        switch (webSocketEvent.getType()) {
            case WebSocketEvent.TYPE_QMS:
                String nick = notificationEvent.getUserNick();
                if (nick == null || nick.isEmpty()) {
                    return "Сообщения 4PDA";
                }
                break;
            case WebSocketEvent.TYPE_SITE:
                return "ForPDA";
        }
        return notificationEvent.getUserNick();
    }

    public String generateContentText(NotificationEvent notificationEvent) {
        WebSocketEvent webSocketEvent = notificationEvent.getWebSocketEvent();
        switch (webSocketEvent.getType()) {
            case WebSocketEvent.TYPE_QMS:
                return notificationEvent.getThemeTitle() + ": " + notificationEvent.getMessageCount() + " непрочитанных сообщений";
            case WebSocketEvent.TYPE_THEME:
                if (webSocketEvent.getEventCode() == WebSocketEvent.EVENT_MENTION) {
                    return "Ответил Вам в теме \"" + notificationEvent.getThemeTitle() + "\"";
                } else {
                    return "Написал сообщение в теме \"" + notificationEvent.getThemeTitle() + "\"";
                }
            case WebSocketEvent.TYPE_SITE:
                return "Вам ответили на комментарий на сайте";
        }
        return "Title";
    }

    public String generateSummaryText(NotificationEvent notificationEvent) {
        WebSocketEvent webSocketEvent = notificationEvent.getWebSocketEvent();
        if (webSocketEvent.getEventCode() == WebSocketEvent.EVENT_MENTION) {
            return "Упоминание";
        }
        switch (webSocketEvent.getType()) {
            case WebSocketEvent.TYPE_QMS:
                return "Чат QMS";
            case WebSocketEvent.TYPE_THEME:
                return "Избранное";
            case WebSocketEvent.TYPE_SITE:
                return "Комментарий";
        }
        return "Summary";
    }

    public String generateIntentUrl(NotificationEvent notificationEvent) {
        WebSocketEvent webSocketEvent = notificationEvent.getWebSocketEvent();
        if (webSocketEvent.getEventCode() == WebSocketEvent.EVENT_MENTION) {
            switch (webSocketEvent.getType()) {
                case WebSocketEvent.TYPE_THEME:
                    return "http://4pda.ru/forum/index.php?showtopic=" + notificationEvent.getThemeId() + "&view=findpost&p=" + webSocketEvent.getMessageId();
                case WebSocketEvent.TYPE_SITE:
                    return "http://4pda.ru/index.php?p=" + notificationEvent.getThemeId() + "/#comment" + webSocketEvent.getMessageId();
            }
        }
        switch (webSocketEvent.getType()) {
            case WebSocketEvent.TYPE_QMS:
                return "http://4pda.ru/forum/index.php?act=qms&mid=" + notificationEvent.getUserId() + "&t=" + notificationEvent.getThemeId();
            case WebSocketEvent.TYPE_THEME:
                return "http://4pda.ru/forum/index.php?showtopic=" + notificationEvent.getThemeId() + "&view=getnewpost";
            /*case WebSocketEvent.TYPE_SITE:
                return "Комментарий";*/
        }
        return "Summary";
    }

}
