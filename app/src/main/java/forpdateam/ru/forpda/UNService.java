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
import forpdateam.ru.forpda.api.events.models.UniversalEvent;
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
 * Created by radiationx on 31.07.17.
 */

public class UNService extends Service {
    private final static int NOTIFY_STACKED_QMS_ID = -123;
    private final static int NOTIFY_STACKED_FAV_ID = -234;
    public final static String CHECK_LAST_EVENTS = "SOSNI_HUICA_DOZE";
    private NotificationManagerCompat mNotificationManager;
    private SparseArray<UniversalEvent> eventsHistory = new SparseArray<>();
    private WebSocket webSocket;
    private long lastHardCheckTime = 0;

    /*private boolean notificationsEnabled = Preferences.Notifications.Main.isEnabled();
    private boolean favoritesEnabled = Preferences.Notifications.Main.isEnabled();
    private boolean mentionsEnabled = Preferences.Notifications.Main.isEnabled();
    private boolean qmsEnabled = Preferences.Notifications.Main.isEnabled();*/

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
                    handleEvent(UniversalEvent.Source.THEME);
                }
                break;
            }
            case Preferences.Notifications.Qms.ENABLED: {
                if (Preferences.Notifications.Qms.isEnabled()) {
                    handleEvent(UniversalEvent.Source.QMS);
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
            UniversalEvent event = Api.UniversalEvents().parseWebSocketEvent(matcher);
            if (event != null) {
                if (event.getEvent() != UniversalEvent.Event.HAT_EDITED) {
                    handleWebSocketEvent(event);
                }
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
            if (UNService.this.webSocket != null) {
                UNService.this.webSocket.cancel();
                UNService.this.webSocket = null;
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
                handleEvent(UniversalEvent.Source.THEME);
                handleEvent(UniversalEvent.Source.QMS);
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

    private void handleWebSocketEvent(UniversalEvent event) {
        if (event.isRead()) {
            UniversalEvent oldEvent = eventsHistory.get(event.notifyId(UniversalEvent.Event.NEW));

            if (event.fromTheme()) {
                //Убираем уведомления избранного
                if (oldEvent != null && event.getMessageId() >= oldEvent.getMessageId()) {
                    mNotificationManager.cancel(oldEvent.notifyId());
                }

                //Убираем уведомление упоминаний
                oldEvent = eventsHistory.get(event.notifyId(UniversalEvent.Event.MENTION));
                if (oldEvent != null) {
                    mNotificationManager.cancel(oldEvent.notifyId());
                }
            } else if (event.fromQms()) {

                //Убираем уведомление кумыса
                if (oldEvent != null) {
                    mNotificationManager.cancel(oldEvent.notifyId());
                }
            }
            eventsHistory.remove(event.notifyId(UniversalEvent.Event.NEW));
            return;
        }
        handleEvent(event);
    }

    private void handleEvent(UniversalEvent.Source source) {
        handleEvent(null, source);
    }

    private void handleEvent(UniversalEvent event) {
        handleEvent(event, event.getSource());
    }

    private void handleEvent(@Nullable UniversalEvent event, UniversalEvent.Source source) {
        if (!Preferences.Notifications.Main.isEnabled()) {
            return;
        }

        if (UniversalEvent.fromSite(source)) {
            if (Preferences.Notifications.Mentions.isEnabled()) {
                sendNotification(event);
            }
            return;
        }
        if (UniversalEvent.fromQms(source)) {
            if (!Preferences.Notifications.Qms.isEnabled()) {
                return;
            }
        } else if (UniversalEvent.fromTheme(source)) {
            if (event != null && event.isMention()) {
                if (!Preferences.Notifications.Mentions.isEnabled()) {
                    return;
                }
            } else {
                if (!Preferences.Notifications.Favorites.isEnabled()) {
                    return;
                }
            }
        }

        Log.d("pizdec", "handle event " + event + " : " + source);
        if (event != null) {
            Log.d("pizdec", "handle event UE: " + event.getSourceId() + " : " + event.getEvent() + " : " + event.getSource());
        }

        loadEvents(loadedEvents -> {
            List<UniversalEvent> savedEvents = getSavedEvents(source);
            //savedEvents = new ArrayList<>();
            saveEvents(loadedEvents, source);
            List<UniversalEvent> newEvents = compareEvents(savedEvents, loadedEvents, source);
            List<UniversalEvent> stackedNewEvents = new ArrayList<>(newEvents);

            if (event != null) {
                //Удаляем из общего уведомления текущее уведомление
                for (UniversalEvent newEvent : newEvents) {
                    Log.d("pizdec", "handle newEvent UE: " + newEvent.getSourceId() + " : " + newEvent.getEvent() + " : " + newEvent.getSource());

                    if (newEvent.getSourceId() == event.getSourceId()) {
                        stackedNewEvents.remove(newEvent);
                        if (newEvent.getEvent() != event.getEvent()) {
                            newEvent.setEvent(event.getEvent());
                        }
                        sendNotification(newEvent);
                    } else if (event.isMention() && !Preferences.Notifications.Favorites.isEnabled()) {
                        stackedNewEvents.remove(newEvent);
                    }
                }
            }
            Log.d("pizdec", "handle stacked newEvent size" + stackedNewEvents.size());
            for (UniversalEvent newEvent : stackedNewEvents) {
                Log.d("pizdec", "handle stacked newEvent UE: " + newEvent.getSourceId() + " : " + newEvent.getEvent() + " : " + newEvent.getSource());
            }

            sendNotifications(stackedNewEvents);
        }, source);
    }

    private List<UniversalEvent> getSavedEvents(UniversalEvent.Source source) {
        String prefKey = "";
        if (UniversalEvent.fromQms(source)) {
            prefKey = Preferences.Notifications.Data.QMS_EVENTS;
        } else if (UniversalEvent.fromTheme(source)) {
            prefKey = Preferences.Notifications.Data.FAVORITES_EVENTS;
        }

        Set<String> savedEvents = App.getInstance().getPreferences().getStringSet(prefKey, new ArraySet<>());
        StringBuilder responseBuilder = new StringBuilder();
        for (String saved : savedEvents) {
            responseBuilder.append(saved).append('\n');
        }
        String response = responseBuilder.toString();

        if (UniversalEvent.fromQms(source)) {
            return Api.UniversalEvents().getQmsEvents(response);
        } else if (UniversalEvent.fromTheme(source)) {
            return Api.UniversalEvents().getFavoritesEvents(response);
        }
        return new ArrayList<>();
    }

    private void saveEvents(List<UniversalEvent> loadedEvents, UniversalEvent.Source source) {
        String prefKey = "";
        if (UniversalEvent.fromQms(source)) {
            prefKey = Preferences.Notifications.Data.QMS_EVENTS;
        } else if (UniversalEvent.fromTheme(source)) {
            prefKey = Preferences.Notifications.Data.FAVORITES_EVENTS;
        }

        Set<String> savedEvents = new ArraySet<>();
        for (UniversalEvent event : loadedEvents) {
            savedEvents.add(event.getSourceEventText());
        }
        App.getInstance().getPreferences().edit().putStringSet(prefKey, savedEvents).apply();
    }

    private List<UniversalEvent> compareEvents(List<UniversalEvent> savedEvents, List<UniversalEvent> loadedEvents, UniversalEvent.Source source) {
        List<UniversalEvent> resultEvents = new ArrayList<>();

        boolean onlyImportant = false;
        if (UniversalEvent.fromTheme(source)) {
            onlyImportant = Preferences.Notifications.Favorites.isOnlyImportant();
        }

        for (UniversalEvent loaded : loadedEvents) {
            boolean isNew = true;
            for (UniversalEvent saved : savedEvents) {
                if (loaded.getSourceId() == saved.getSourceId()) {
                    if (loaded.getTimeStamp() <= saved.getTimeStamp()) {
                        isNew = false;
                    }
                }
            }

            if (onlyImportant) {
                if (!loaded.isImportant()) {
                    isNew = false;
                }
            }

            if (isNew) {
                resultEvents.add(loaded);
            }
        }

        return resultEvents;
    }

    private void loadEvents(Consumer<List<UniversalEvent>> consumer, UniversalEvent.Source source) {
        Observable<List<UniversalEvent>> observable = null;
        if (source == UniversalEvent.Source.QMS) {
            observable = Observable.fromCallable(() -> Api.UniversalEvents().getQmsEvents());
        } else if (source == UniversalEvent.Source.THEME) {
            observable = Observable.fromCallable(() -> Api.UniversalEvents().getFavoritesEvents());
        }

        if (observable != null) {
            observable
                    .onErrorReturnItem(new ArrayList<>())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(consumer);
        }
    }

/*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
* */

    public Bitmap loadAvatar(UniversalEvent event) throws Exception {
        Bitmap bitmap = null;
        if (!event.fromSite()) {
            ForumUser forumUser = ForumUsersCache.getUserById(event.getUserId());
            Log.d("WS_USER", "FORUM USER CACHE " + forumUser);
            if (forumUser == null) {
                forumUser = ForumUsersCache.loadUserByNick(event.getUserNick());
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

    public void sendNotification(UniversalEvent event, Bitmap avatar) {
        eventsHistory.put(event.notifyId(), event);


        String title = createTitle(event);
        String text = createContent(event);
        String summaryText = createSummary(event);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(text);
        bigTextStyle.setSummaryText(summaryText);

        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(this);

        if (avatar != null && !event.fromSite()) {
            builder.setLargeIcon(avatar);
        }
        builder.setSmallIcon(createSmallIcon(event));

        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setStyle(bigTextStyle);


        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setData(Uri.parse(createIntentUrl(event)));
        notifyIntent.setAction(Intent.ACTION_VIEW);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
        builder.setContentIntent(notifyPendingIntent);

        builder.setAutoCancel(true);

        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setCategory(NotificationCompat.CATEGORY_SOCIAL);


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
        builder.setDefaults(defaults);

        mNotificationManager.cancel(event.notifyId());
        mNotificationManager.notify(event.notifyId(), builder.build());
    }

    public void sendNotification(UniversalEvent event) {
        if (event.getUserId() == ClientHelper.getUserId()) {
            return;
        }

        if (Preferences.Notifications.Main.isAvatarsEnabled()) {
            Observable.fromCallable(() -> loadAvatar(event))
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
                    .subscribe(avatar -> sendNotification(event, avatar));
        } else {
            sendNotification(event, null);
        }
    }


    public void sendNotifications(List<UniversalEvent> events) {
        if (events.size() == 0) {
            return;
        }
        if (events.size() == 1) {
            sendNotification(events.get(0));
            return;
        }
        // WebSocketEvent webSocketEvent = notificationEvent.getWebSocketEvent();


        String title = createStackedTitle(events);
        CharSequence text = createStackedContent(events);
        String summaryText = createStackedSummary(events);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(text);
        bigTextStyle.setSummaryText(summaryText);

        NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle("SU4KA");
        messagingStyle.setConversationTitle("CONV TITLE");
        for (UniversalEvent event : events) {
            messagingStyle.addMessage(event.getSourceTitle(), event.getTimeStamp(), event.getUserNick());
        }


        NotificationCompat.Builder mBuilder;
        mBuilder = new NotificationCompat.Builder(this);


        mBuilder.setSmallIcon(createStackedSmallIcon(events));

        mBuilder.setContentTitle(title);
        mBuilder.setContentText(text);
        mBuilder.setStyle(bigTextStyle);


        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setData(Uri.parse(createStackedIntentUrl(events)));
        notifyIntent.setAction(Intent.ACTION_VIEW);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
        mBuilder.setContentIntent(notifyPendingIntent);

        mBuilder.setAutoCancel(true);

        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setCategory(NotificationCompat.CATEGORY_SOCIAL);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_SOUND);

        int id = 0;
        UniversalEvent event = events.get(0);
        if (event.fromQms()) {
            id = NOTIFY_STACKED_QMS_ID;
        } else if (event.fromTheme()) {
            id = NOTIFY_STACKED_FAV_ID;
        }
        mNotificationManager.notify(id, mBuilder.build());
    }


    /*
    * DEFAULT EVENT
    * */

    @DrawableRes
    public int createSmallIcon(UniversalEvent event) {
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

    public String createTitle(UniversalEvent event) {
        if (event.fromQms()) {
            String nick = event.getUserNick();
            if (nick == null || nick.isEmpty())
                return "Сообщения 4PDA";
        }

        if (event.fromSite())
            return "ForPDA";

        return event.getUserNick();
    }

    public String createContent(UniversalEvent event) {
        if (event.fromQms())
            return event.getSourceTitle() + ": " + event.getMsgCount() + " непрочитанных сообщений";

        if (event.fromTheme()) {
            if (event.isMention())
                return "Ответил Вам в теме \"" + event.getSourceTitle() + "\"";

            return "Написал сообщение в теме \"" + event.getSourceTitle() + "\"";
        }

        if (event.fromSite())
            return "Вам ответили на комментарий на сайте";

        return "";
    }

    public String createSummary(UniversalEvent event) {
        if (event.isMention())
            return "Упоминание";

        if (event.fromQms())
            return "Чат QMS";

        if (event.fromTheme())
            return "Избранное";

        if (event.fromSite())
            return "Комментарий";

        return "";
    }

    public String createIntentUrl(UniversalEvent event) {
        if (event.isMention()) {
            if (event.fromTheme())
                return "http://4pda.ru/forum/index.php?showtopic=" + event.getSourceId() + "&view=findpost&p=" + event.getMessageId();

            if (event.fromSite())
                return "http://4pda.ru/index.php?p=" + event.getSourceId() + "/#comment" + event.getMessageId();
        }

        if (event.fromQms())
            return "http://4pda.ru/forum/index.php?act=qms&mid=" + event.getUserId() + "&t=" + event.getSourceId();

        if (event.fromTheme())
            return "http://4pda.ru/forum/index.php?showtopic=" + event.getSourceId() + "&view=getnewpost";

        return "";
    }


    /*
    * STACKED EVENTS
    * */
    private String createStackedTitle(List<UniversalEvent> events) {
        return createStackedSummary(events);
    }

    private CharSequence createStackedContent(List<UniversalEvent> events) {
        StringBuilder content = new StringBuilder();

        final int maxCount = 4;
        int size = Math.min(events.size(), maxCount);
        for (int i = 0; i < size; i++) {
            UniversalEvent event = events.get(i);
            if (event.fromQms()) {
                content.append("<b>").append(event.getUserNick()).append("</b>");
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

        return Utils.spannedFromHtml(content.toString());
    }

    private String createStackedSummary(List<UniversalEvent> events) {
        return createSummary(events.get(0));
    }

    @DrawableRes
    public int createStackedSmallIcon(List<UniversalEvent> events) {
        return createSmallIcon(events.get(0));
    }

    private String createStackedIntentUrl(List<UniversalEvent> events) {
        UniversalEvent event = events.get(0);
        if (event.fromQms())
            return "http://4pda.ru/forum/index.php?act=qms";

        if (event.fromTheme())
            return "http://4pda.ru/forum/index.php?act=fav";

        return "";
    }
}
