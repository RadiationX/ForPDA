package forpdateam.ru.forpda;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.util.ArraySet;
import android.support.v4.util.Pair;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.events.Events;
import forpdateam.ru.forpda.api.others.user.ForumUser;
import forpdateam.ru.forpda.api.events.models.NotificationEvent;
import forpdateam.ru.forpda.api.events.models.WebSocketEvent;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.rxapi.ForumUsersCache;
import forpdateam.ru.forpda.utils.BitmapUtils;
import forpdateam.ru.forpda.utils.Html;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by radiationx on 07.07.17.
 */

public class NotificationsService extends Service {
    private final static int NOTIFY_STACKED_QMS_ID = -123;
    private final static int NOTIFY_STACKED_FAV_ID = -234;
    public final static String CHECK_LAST_EVENTS = "SOSNI_HUICA_DOZE";
    private NotificationManagerCompat mNotificationManager;
    private SparseArray<WebSocketEvent> notificationEvents = new SparseArray<>();
    private WebSocket webSocket;

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
            NotificationsService.this.webSocket.cancel();
            NotificationsService.this.webSocket = null;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i("WS_SERVICE", "Service: onCreate " + this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("WS_SERVICE", "Service: onStartCommand " + flags + " : " + startId + " : " + intent);
        Log.i("WS_SERVICE", "Service: onStartCommand " + webSocket);
        if (webSocket == null) {
            webSocket = Client.getInstance().createWebSocketConnection(webSocketListener);
        }
        if (mNotificationManager == null) {
            mNotificationManager = NotificationManagerCompat.from(this);
        }
        webSocket.send("[0,\"sv\"]");
        webSocket.send("[0, \"ea\", \"u" + ClientHelper.getUserId() + "\"]");
        /*new Handler().postDelayed(() -> {
            webSocketListener.onMessage(webSocket, "[30309,0,\"s344799\",3,3977242]");
        }, 5000);*/
        if (intent != null && intent.getAction() != null && intent.getAction().equals(CHECK_LAST_EVENTS)) {
            Log.d("WS_SERVICE", "HANDLE CHECK LAST EVENTS");
            checkLastEvents();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("WS_SERVICE", "Service: onDestroy");
        if (webSocket != null)
            webSocket.close(1000, null);
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("WS_SERVICE", "Service: onTaskRemoved");
        if (webSocket != null) {
            webSocket.close(1000, null);
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Intent restartIntent = new Intent(this, getClass());

            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent pi = PendingIntent.getService(this, 1, restartIntent, PendingIntent.FLAG_ONE_SHOT);
            restartIntent.putExtra("RESTART", "RESTART_CHEBUREK");
            am.setExact(AlarmManager.RTC, System.currentTimeMillis() + 3000, pi);
        }
    }

    private void checkLastEvents() {


        loadQmsEvents(notificationEvents1 -> {
            List<NotificationEvent> oldQmsEvents = getOldEvents(WebSocketEvent.TYPE_QMS);
            oldQmsEvents = new ArrayList<>();
            compareEvents(oldQmsEvents, notificationEvents1, WebSocketEvent.TYPE_QMS);
            Set<String> newSavedQmsEvents = new ArraySet<>();
            for (NotificationEvent event : notificationEvents1) {
                newSavedQmsEvents.add(event.getSource());
            }
            App.getInstance().getPreferences().edit().putStringSet("test.notifications.qms_events", newSavedQmsEvents).apply();
        });

        loadFavEvents(notificationEvents1 -> {
            List<NotificationEvent> oldFavEvents = getOldEvents(WebSocketEvent.TYPE_THEME);
            oldFavEvents = new ArrayList<>();
            compareEvents(oldFavEvents, notificationEvents1, WebSocketEvent.TYPE_THEME);
            Set<String> newSavedQmsEvents = new ArraySet<>();
            for (NotificationEvent event : notificationEvents1) {
                newSavedQmsEvents.add(event.getSource());
            }
            App.getInstance().getPreferences().edit().putStringSet("test.notifications.qms_events", newSavedQmsEvents).apply();
        });


    }

    private String getOldEventsSource(int type) {
        String prefKey = "";
        if (type == WebSocketEvent.TYPE_QMS) {
            prefKey = "test.notifications.events_qms";
        } else if (type == WebSocketEvent.TYPE_THEME) {
            prefKey = "test.notifications.events_fav";
        }

        Set<String> oldSavedEvents = App.getInstance().getPreferences().getStringSet(prefKey, new ArraySet<>());
        StringBuilder response = new StringBuilder();
        for (String source : oldSavedEvents) {
            response.append(source).append('\n');
        }
        return response.toString();
    }

    private List<NotificationEvent> getOldEvents(int type) {
        String response = getOldEventsSource(type);
        return Api.Events().getQmsEvents(response);
    }

    private void compareEvents(List<NotificationEvent> oldEvents, List<NotificationEvent> newEvents, int type) {
        List<NotificationEvent> resultEvents = new ArrayList<>();
        for (NotificationEvent newEvent : newEvents) {
            boolean isNew = true;
            for (NotificationEvent oldEvent : oldEvents) {
                if (newEvent.getThemeId() == oldEvent.getThemeId()) {
                    if (newEvent.getTimeStamp() <= oldEvent.getTimeStamp()) {
                        isNew = false;
                    }
                }
            }
            if (isNew) {
                WebSocketEvent webSocketEvent = new WebSocketEvent();
                webSocketEvent.setId(newEvent.getThemeId());
                webSocketEvent.setType(type);
                webSocketEvent.setEventCode(WebSocketEvent.EVENT_NEW);
                newEvent.setWebSocketEvent(webSocketEvent);
                resultEvents.add(newEvent);
            }
        }
        /*Log.d("WS_CHECK", "compareEvents " + resultEvents.size());
        for (NotificationEvent event : resultEvents) {
            sendNotification(event);
        }*/
        sendNotifications(resultEvents);


    }

    private String generateStackedTitle(List<NotificationEvent> notificationEvents) {
        return generateStackedSummary(notificationEvents);
    }

    private CharSequence generateStackedContent(List<NotificationEvent> notificationEvents) {
        StringBuilder content = new StringBuilder();

        final int maxCount = 4;
        int size = Math.min(notificationEvents.size(), maxCount);
        for (int i = 0; i < size; i++) {
            NotificationEvent event = notificationEvents.get(i);


            WebSocketEvent webSocketEvent = event.getWebSocketEvent();
            if (webSocketEvent.getType() == WebSocketEvent.TYPE_QMS) {
                content.append("<b>").append(event.getUserNick()).append("</b>");
                content.append(": ").append(event.getThemeTitle());
            } else if (webSocketEvent.getType() == WebSocketEvent.TYPE_THEME) {
                content.append(event.getThemeTitle());
            }
            if (i < size - 1) {
                content.append("<br>");
            }
        }

        if (notificationEvents.size() > size) {
            content.append("<br>");
            content.append("...и еще ").append(notificationEvents.size() - size);
        }

        return Html.fromHtml(content.toString());
    }

    private String generateStackedSummary(List<NotificationEvent> notificationEvents) {
        return generateSummaryText(notificationEvents.get(0));
    }

    @DrawableRes
    public int generateStackedSmallIcon(List<NotificationEvent> notificationEvents) {
        return generateSmallIcon(notificationEvents.get(0));
    }

    private String generateStackedIntentUrl(List<NotificationEvent> notificationEvents) {
        NotificationEvent notificationEvent = notificationEvents.get(0);
        WebSocketEvent webSocketEvent = notificationEvent.getWebSocketEvent();
        switch (webSocketEvent.getType()) {
            case WebSocketEvent.TYPE_QMS:
                return "http://4pda.ru/forum/index.php?act=qms";
            case WebSocketEvent.TYPE_THEME:
                return "http://4pda.ru/forum/index.php?act=fav";
        }
        return "";
    }

    public void sendNotifications(List<NotificationEvent> notificationEvents) {
        if (notificationEvents.size() == 0) {
            return;
        }
        if (notificationEvents.size() == 1) {
            sendNotification(notificationEvents.get(0));
            return;
        }
        // WebSocketEvent webSocketEvent = notificationEvent.getWebSocketEvent();


        String title = generateStackedTitle(notificationEvents);
        CharSequence text = generateStackedContent(notificationEvents);
        String summaryText = generateStackedSummary(notificationEvents);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(text);
        bigTextStyle.setSummaryText(summaryText);

        NotificationCompat.Builder mBuilder;
        mBuilder = new NotificationCompat.Builder(this);


        mBuilder.setSmallIcon(generateStackedSmallIcon(notificationEvents));

        mBuilder.setContentTitle(title);
        mBuilder.setContentText(text);
        mBuilder.setStyle(bigTextStyle);


        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setData(Uri.parse(generateStackedIntentUrl(notificationEvents)));
        notifyIntent.setAction(Intent.ACTION_VIEW);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
        mBuilder.setContentIntent(notifyPendingIntent);

        mBuilder.setAutoCancel(true);

        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setCategory(NotificationCompat.CATEGORY_SOCIAL);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_SOUND);

        int id = 0;
        WebSocketEvent webSocketEvent = notificationEvents.get(0).getWebSocketEvent();
        if (webSocketEvent.getType() == WebSocketEvent.TYPE_QMS) {
            id = NOTIFY_STACKED_QMS_ID;
        } else if (webSocketEvent.getType() == WebSocketEvent.TYPE_THEME) {
            id = NOTIFY_STACKED_FAV_ID;
        }

        mNotificationManager.notify(id, mBuilder.build());
    }

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

    private void loadQmsEvents(Consumer<List<NotificationEvent>> consumer) {
        Observable.fromCallable(() -> Api.Events().getQmsEvents())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }

    private void loadFavEvents(Consumer<List<NotificationEvent>> consumer) {
        Observable.fromCallable(() -> Api.Events().getFavoritesEvents())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }

    private void handleQmsEvent(WebSocketEvent webSocketEvent) {
        loadQmsEvents(qmsEvents -> {
            for (NotificationEvent notificationEvent : qmsEvents) {
                if (notificationEvent.getThemeId() == webSocketEvent.getId()) {
                    notificationEvent.setWebSocketEvent(webSocketEvent);
                    sendNotification(notificationEvent);
                }
            }
        });
    }

    private void handleFavEvent(WebSocketEvent webSocketEvent) {
        loadFavEvents(favEvents -> {
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
                    mBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_SOUND);

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
        return "";
    }


}