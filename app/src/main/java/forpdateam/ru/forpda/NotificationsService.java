package forpdateam.ru.forpda;

import android.app.AlarmManager;
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
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.Set;
import java.util.regex.Matcher;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.events.Events;
import forpdateam.ru.forpda.api.events.models.NotificationEvent;
import forpdateam.ru.forpda.api.events.models.WebSocketEvent;
import forpdateam.ru.forpda.api.others.user.ForumUser;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.rxapi.ForumUsersCache;
import forpdateam.ru.forpda.settings.Preferences;
import forpdateam.ru.forpda.utils.BitmapUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
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
    private SparseArray<WebSocketEvent> eventsHistory = new SparseArray<>();
    private WebSocket webSocket;
    private long lastHardCheckTime = 0;

    private Observer loginObserver = (observable, o) -> {
        if (o == null) o = false;

    };
    private Observer networkObserver = (observable, o) -> {
        if (o == null) o = true;
        if ((boolean) o) {
            if (Preferences.Notifications.Main.isEnabled()) {
                start(true);
            }
        }
    };

    private Observer notificationSettingObserver = (observable, o) -> {
        if (o == null) return;
        String key = (String) o;
        Log.d("WS_SETTINGS", "KEY: " + key);
        switch (key) {
            case Preferences.Notifications.Main.ENABLED: {
                if (Preferences.Notifications.Main.isEnabled()) {
                    start(true);
                } else {
                    stop();
                }
                break;
            }
            case Preferences.Notifications.Favorites.ENABLED: {
                if (Preferences.Notifications.Favorites.isEnabled()) {
                    handleEvent(WebSocketEvent.TYPE_THEME);
                }
                break;
            }
            case Preferences.Notifications.Qms.ENABLED: {
                if (Preferences.Notifications.Qms.isEnabled()) {
                    handleEvent(WebSocketEvent.TYPE_QMS);
                }
                break;
            }
            /*case Preferences.Notifications.Mentions.ENABLED: {

                break;
            }*/
        }
    };

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
            if (NotificationsService.this.webSocket != null) {
                NotificationsService.this.webSocket.cancel();
                NotificationsService.this.webSocket = null;
            }
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
        Client.getInstance().addNetworkObserver(networkObserver);
        App.getInstance().addPreferenceChangeObserver(notificationSettingObserver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("WS_SERVICE", "Service: onStartCommand " + flags + " : " + startId + " : " + intent);
        Log.i("WS_SERVICE", "Service: onStartCommand " + webSocket);
        if (mNotificationManager == null) {
            mNotificationManager = NotificationManagerCompat.from(this);
        }
        if (Preferences.Notifications.Main.isEnabled()) {
            boolean checkEvents = intent != null && intent.getAction() != null && intent.getAction().equals(CHECK_LAST_EVENTS);
            long time = System.currentTimeMillis();

            Log.d("WS_SERVICE", "HANDLE CHECK LAST EVENTS: " + time + " : " + lastHardCheckTime + " : " + (time - lastHardCheckTime));

            if (checkEvents && ((time - lastHardCheckTime) >= 1000 * 60 * 1)) {
                lastHardCheckTime = time;
                checkEvents = true;
            } else {
                checkEvents = false;
            }
            start(checkEvents);
        }
        return START_STICKY;
    }

    private void start(boolean checkEvents) {
        if (Client.getInstance().getNetworkState()) {
            if (webSocket == null) {
                webSocket = Client.getInstance().createWebSocketConnection(webSocketListener);
            }
            webSocket.send("[0,\"sv\"]");
            webSocket.send("[0, \"ea\", \"u" + ClientHelper.getUserId() + "\"]");
            if (checkEvents) {
                handleEvent(WebSocketEvent.TYPE_QMS);
                handleEvent(WebSocketEvent.TYPE_THEME);
            }
        }
    }

    private void stop() {
        if (webSocket != null)
            webSocket.close(1000, null);
        webSocket = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getInstance().removePreferenceChangeObserver(notificationSettingObserver);
        Client.getInstance().removeNetworkObserver(networkObserver);
        Log.i("WS_SERVICE", "Service: onDestroy");
        stop();
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


    private void handleWebSocketEvent(WebSocketEvent webSocketEvent) {
        if (webSocketEvent.getEventCode() == WebSocketEvent.EVENT_READ) {
            WebSocketEvent oldWebSocketEvent = eventsHistory.get(webSocketEvent.createNotificationId(WebSocketEvent.EVENT_NEW));

            if (webSocketEvent.getType() == WebSocketEvent.TYPE_THEME) {
                //Убираем уведомления избранного
                if (oldWebSocketEvent != null && webSocketEvent.getMessageId() >= oldWebSocketEvent.getMessageId()) {
                    mNotificationManager.cancel(oldWebSocketEvent.createNotificationId());
                }

                //Убираем уведомление упоминаний
                oldWebSocketEvent = eventsHistory.get(webSocketEvent.createNotificationId(WebSocketEvent.EVENT_MENTION));
                if (oldWebSocketEvent != null) {
                    mNotificationManager.cancel(oldWebSocketEvent.createNotificationId());
                }
            } else if (webSocketEvent.getType() == WebSocketEvent.TYPE_QMS) {

                //Убираем уведомление кумыса
                if (oldWebSocketEvent != null) {
                    mNotificationManager.cancel(oldWebSocketEvent.createNotificationId());
                }
            }
            eventsHistory.remove(webSocketEvent.createNotificationId(WebSocketEvent.EVENT_NEW));
        } else {
            switch (webSocketEvent.getType()) {
                case WebSocketEvent.TYPE_QMS:
                    if (webSocketEvent.getEventCode() == WebSocketEvent.EVENT_NEW) {
                        handleEvent(webSocketEvent);
                    }
                    break;
                case WebSocketEvent.TYPE_THEME:
                    if (webSocketEvent.getEventCode() == WebSocketEvent.EVENT_NEW || webSocketEvent.getEventCode() == WebSocketEvent.EVENT_MENTION) {
                        handleEvent(webSocketEvent);
                    }
                    break;
                case WebSocketEvent.TYPE_SITE:
                    if (webSocketEvent.getEventCode() == WebSocketEvent.EVENT_MENTION) {
                        handleEvent(webSocketEvent);
                    }
                    break;
            }
        }
    }

    private void loadEvents(Consumer<List<NotificationEvent>> consumer, int type) {
        Observable<List<NotificationEvent>> observable = null;
        if (type == WebSocketEvent.TYPE_QMS) {
            observable = Observable.fromCallable(() -> Api.Events().getQmsEvents());
        } else if (type == WebSocketEvent.TYPE_THEME) {
            observable = Observable.fromCallable(() -> Api.Events().getFavoritesEvents());
        }

        if (observable != null) {
            observable
                    .onErrorReturnItem(new ArrayList<>())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(consumer);
        }
    }

    private void handleEvent(int type) {
        handleEvent(null, type);
    }

    private void handleEvent(WebSocketEvent webSocketEvent) {
        handleEvent(webSocketEvent, webSocketEvent.getType());
    }

    private void handleEvent(WebSocketEvent webSocketEvent, int type) {
        if (type == WebSocketEvent.TYPE_SITE) {
            NotificationEvent notificationEvent = new NotificationEvent();
            notificationEvent.setWebSocketEvent(webSocketEvent);
            notificationEvent.setThemeId(webSocketEvent.getId());
            sendNotification(notificationEvent);
            return;
        }
        if (type == WebSocketEvent.TYPE_QMS) {
            if (!Preferences.Notifications.Qms.isEnabled()) {
                return;
            }
        } else if (type == WebSocketEvent.TYPE_THEME) {
            if (webSocketEvent != null && webSocketEvent.getEventCode() == WebSocketEvent.EVENT_MENTION) {
                if (!Preferences.Notifications.Mentions.isEnabled()) {
                    return;
                }
            } else {
                if(!Preferences.Notifications.Favorites.isEnabled()){
                    return;
                }
            }
        }

        loadEvents(loadedEvents -> {
            List<NotificationEvent> savedEvents = getSavedEvents(type);
            //savedEvents = new ArrayList<NotificationEvent>();
            saveEvents(loadedEvents, type);
            List<NotificationEvent> newEvents = compareEvents(savedEvents, loadedEvents, type);
            List<NotificationEvent> stackedNewEvents = new ArrayList<>(newEvents);

            if (webSocketEvent != null) {
                for (NotificationEvent newEvent : newEvents) {
                    if (newEvent.getThemeId() == webSocketEvent.getId()) {
                        newEvent.setWebSocketEvent(webSocketEvent);
                        stackedNewEvents.remove(newEvent);
                        sendNotification(newEvent);
                    }
                }
            }
            sendNotifications(stackedNewEvents);
        }, type);
    }

    private List<NotificationEvent> getSavedEvents(int type) {
        String prefKey = "";
        if (type == WebSocketEvent.TYPE_QMS) {
            prefKey = Preferences.Notifications.Data.QMS_EVENTS;
        } else if (type == WebSocketEvent.TYPE_THEME) {
            prefKey = Preferences.Notifications.Data.FAVORITES_EVENTS;
        }

        Set<String> oldSavedEvents = App.getInstance().getPreferences().getStringSet(prefKey, new ArraySet<>());
        StringBuilder responseBuilder = new StringBuilder();
        for (String source : oldSavedEvents) {
            responseBuilder.append(source).append('\n');
        }
        String response = responseBuilder.toString();

        if (type == WebSocketEvent.TYPE_QMS) {
            return Api.Events().getQmsEvents(response);
        } else if (type == WebSocketEvent.TYPE_THEME) {
            return Api.Events().getFavoritesEvents(response);
        }
        return new ArrayList<>();
    }

    private void saveEvents(List<NotificationEvent> loadedEvents, int type) {
        String prefKey = "";
        if (type == WebSocketEvent.TYPE_QMS) {
            prefKey = Preferences.Notifications.Data.QMS_EVENTS;
        } else if (type == WebSocketEvent.TYPE_THEME) {
            prefKey = Preferences.Notifications.Data.FAVORITES_EVENTS;
        }

        Set<String> savedEvents = new ArraySet<>();
        for (NotificationEvent event : loadedEvents) {
            savedEvents.add(event.getSource());
        }
        App.getInstance().getPreferences().edit().putStringSet(prefKey, savedEvents).apply();
    }

    private List<NotificationEvent> compareEvents(List<NotificationEvent> oldEvents, List<NotificationEvent> newEvents, int type) {
        boolean onlyImportant = false;
        if (type == WebSocketEvent.TYPE_THEME) {
            onlyImportant = Preferences.Notifications.Favorites.isOnlyImportant();
        }

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

            if (onlyImportant) {
                if (!newEvent.isImportant()) {
                    isNew = false;
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

        return resultEvents;
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


        return bitmap;
    }

    public void sendNotification(NotificationEvent notificationEvent, Bitmap avatar) {
        WebSocketEvent webSocketEvent = notificationEvent.getWebSocketEvent();
        eventsHistory.put(webSocketEvent.createNotificationId(), webSocketEvent);


        String title = createTitle(notificationEvent);
        String text = createContent(notificationEvent);
        String summaryText = createSummary(notificationEvent);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(text);
        bigTextStyle.setSummaryText(summaryText);

        NotificationCompat.Builder mBuilder;
        mBuilder = new NotificationCompat.Builder(this);

        if (avatar != null && webSocketEvent.getType() != WebSocketEvent.TYPE_SITE) {
            mBuilder.setLargeIcon(avatar);
        }
        mBuilder.setSmallIcon(createSmallIcon(notificationEvent));

        mBuilder.setContentTitle(title);
        mBuilder.setContentText(text);
        mBuilder.setStyle(bigTextStyle);


        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setData(Uri.parse(createIntentUrl(notificationEvent)));
        notifyIntent.setAction(Intent.ACTION_VIEW);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
        mBuilder.setContentIntent(notifyPendingIntent);

        mBuilder.setAutoCancel(true);

        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setCategory(NotificationCompat.CATEGORY_SOCIAL);


        int defaults = 0;
        if (Preferences.Notifications.Main.isSoundEnabled()) {
            defaults |= NotificationCompat.DEFAULT_SOUND;
        }
        if (Preferences.Notifications.Main.isVibrationEnabled()) {
            defaults |= NotificationCompat.DEFAULT_VIBRATE;
        }
        if (Preferences.Notifications.Main.isIndicatorEnabled()) {
            defaults |= NotificationCompat.DEFAULT_LIGHTS;
        }
        mBuilder.setDefaults(defaults);

        mNotificationManager.cancel(webSocketEvent.createNotificationId());
        mNotificationManager.notify(webSocketEvent.createNotificationId(), mBuilder.build());
    }

    public void sendNotification(NotificationEvent notificationEvent) {
        if (notificationEvent.getUserId() == ClientHelper.getUserId()) {
            return;
        }

        if (Preferences.Notifications.Main.isAvatarsEnabled()) {
            Observable.fromCallable(() -> loadAvatar(notificationEvent))
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
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(avatar -> sendNotification(notificationEvent, avatar));
        } else {
            sendNotification(notificationEvent, null);
        }
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


        String title = createStackedTitle(notificationEvents);
        CharSequence text = createStackedContent(notificationEvents);
        String summaryText = createStackedSummary(notificationEvents);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(text);
        bigTextStyle.setSummaryText(summaryText);

        NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle("SU4KA");
        messagingStyle.setConversationTitle("CONV TITLE");
        for (NotificationEvent event : notificationEvents) {
            messagingStyle.addMessage(event.getThemeTitle(), event.getTimeStamp(), event.getUserNick());
        }


        NotificationCompat.Builder mBuilder;
        mBuilder = new NotificationCompat.Builder(this);


        mBuilder.setSmallIcon(createStackedSmallIcon(notificationEvents));

        mBuilder.setContentTitle(title);
        mBuilder.setContentText(text);
        mBuilder.setStyle(bigTextStyle);


        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setData(Uri.parse(createStackedIntentUrl(notificationEvents)));
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


    /*
    * DEFAULT EVENT
    * */

    @DrawableRes
    public int createSmallIcon(NotificationEvent notificationEvent) {
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

    public String createTitle(NotificationEvent notificationEvent) {
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

    public String createContent(NotificationEvent notificationEvent) {
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

    public String createSummary(NotificationEvent notificationEvent) {
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

    public String createIntentUrl(NotificationEvent notificationEvent) {
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
        }
        return "";
    }


    /*
    * STACKED EVENTS
    * */
    private String createStackedTitle(List<NotificationEvent> notificationEvents) {
        return createStackedSummary(notificationEvents);
    }

    private CharSequence createStackedContent(List<NotificationEvent> notificationEvents) {
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

        return Utils.spannedFromHtml(content.toString());
    }

    private String createStackedSummary(List<NotificationEvent> notificationEvents) {
        return createSummary(notificationEvents.get(0));
    }

    @DrawableRes
    public int createStackedSmallIcon(List<NotificationEvent> notificationEvents) {
        return createSmallIcon(notificationEvents.get(0));
    }

    private String createStackedIntentUrl(List<NotificationEvent> notificationEvents) {
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

}