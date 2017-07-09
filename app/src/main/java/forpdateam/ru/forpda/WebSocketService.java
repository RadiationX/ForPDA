package forpdateam.ru.forpda;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
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
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
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
import forpdateam.ru.forpda.api.others.user.ForumUser;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.rxapi.ForumUsersCache;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by radiationx on 07.07.17.
 */

public class WebSocketService extends Service {
    private final static Pattern inspectorFavoritesPattern = Pattern.compile("(\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) (\\d+)");
    private final static Pattern inspectorQmsPattern = Pattern.compile("(\\d+) \"([\\s\\S]*?)\" (\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) (\\d+)");
    //private SparseArray<NotificationCompat.Builder> notificationBuilders = new SparseArray<>();
    private SparseArray<WebSocketEvent> notificationEvents = new SparseArray<>();

    private class WebSocketEvent {
        /*
        * New - "1" - Qms/Favorites
        * Read - "2" - Qms/Favorites
        * Mention - "3" - Site/Mentions
        * Hat change - "4" - Favorites
        * */
        public final static int EVENT_NEW = 1;
        public final static int EVENT_READ = 2;
        public final static int EVENT_MENTION = 3;
        public final static int EVENT_HAT_CHANGE = 4;

        /*
        * Site - "s"
        * Favorites/Mentions/Themes - "t"
        * Qms chat - "q"
        * */
        public final static int TYPE_SITE = 11;
        public final static int TYPE_THEME = 12;
        public final static int TYPE_QMS = 13;

        /* Unknown field, default: 30309 */
        private int unknown1 = 0;

        /* Unknown field, default: 0 */
        private int unknown2 = 0;

        /* Type: "s"|"t"|"q" */
        private int type = 0;

        /* Theme themeId: Qms|Site|Fav| */
        private int id = 0;

        /* Code: 1|2|3|4 */
        private int eventCode = 0;

        /*
        * QMS, Mention: message/post themeId;
        * Fav: timestamp
        * */
        private int messageId = 0;

        public int getUnknown1() {
            return unknown1;
        }

        public void setUnknown1(int unknown1) {
            this.unknown1 = unknown1;
        }

        public int getUnknown2() {
            return unknown2;
        }

        public void setUnknown2(int unknown2) {
            this.unknown2 = unknown2;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getEventCode() {
            return eventCode;
        }

        public void setEventCode(int eventCode) {
            this.eventCode = eventCode;
        }

        public int getMessageId() {
            return messageId;
        }

        public void setMessageId(int messageId) {
            this.messageId = messageId;
        }

        public String typeName() {
            switch (type) {
                case TYPE_SITE:
                    return "site";
                case TYPE_THEME:
                    return "theme";
                case TYPE_QMS:
                    return "qms";
            }
            return "unknown";
        }

        public String codeName() {
            switch (eventCode) {
                case EVENT_NEW:
                    return "new";
                case EVENT_READ:
                    return "read";
                case EVENT_MENTION:
                    return "mention";
                case EVENT_HAT_CHANGE:
                    return "hat_change";
            }
            return "unknown";
        }

        public int createNotificationId() {
            return createNotificationId(eventCode);
        }

        public int createNotificationId(int eventCodeArg) {
            return (id / 4) + type + eventCodeArg;
        }

        @Override
        public String toString() {
            return "WebSocketEvent {" + "unk1=" + unknown1 + ", unk2=" + unknown2 + "; type=" + typeName() + ", code=" + codeName() + ", themeId=" + id + ", messId=" + messageId + "}";
        }
    }

    public class NotificationEvent {
        private WebSocketEvent webSocketEvent;
        private int themeId = 0;
        private int userId = 0;
        private int timeStamp = 0;

        private String themeTitle = "";
        private String userNick = "";

        //Theme, Mentions?
        private int lastReadTimeStamp = 0;

        //Theme, Mentions?
        private boolean important = false;

        //Theme, Mentions?
        private int messageCount = 0;

        public NotificationEvent(WebSocketEvent webSocketEvent) {
            this.webSocketEvent = webSocketEvent;
        }

        public WebSocketEvent getWebSocketEvent() {
            return webSocketEvent;
        }

        public void setWebSocketEvent(WebSocketEvent webSocketEvent) {
            this.webSocketEvent = webSocketEvent;
        }

        public int getThemeId() {
            return themeId;
        }

        public void setThemeId(int themeId) {
            this.themeId = themeId;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(int timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getThemeTitle() {
            return themeTitle;
        }

        public void setThemeTitle(String themeTitle) {
            this.themeTitle = themeTitle;
        }

        public String getUserNick() {
            return userNick;
        }

        public void setUserNick(String userNick) {
            this.userNick = userNick;
        }

        public int getLastReadTimeStamp() {
            return lastReadTimeStamp;
        }

        public void setReadTimeStamp(int lastReadTimeStamp) {
            this.lastReadTimeStamp = lastReadTimeStamp;
        }

        public boolean isImportant() {
            return important;
        }

        public void setImportant(boolean important) {
            this.important = important;
        }

        public int getMessageCount() {
            return messageCount;
        }

        public void setMessageCount(int messageCount) {
            this.messageCount = messageCount;
        }
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

    public Bitmap getAvatar(NotificationEvent notificationEvent) throws Exception {
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

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        Resources res = this.getResources();
        int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
        int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
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

        Observable.fromCallable(() -> getAvatar(notificationEvent))
                .onErrorReturnItem(ImageLoader.getInstance().loadImageSync("assets://av.png"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    Log.d("WS_RX_BITMAP", "" + bitmap);

                    NotificationCompat.Builder mBuilder;
                    mBuilder = new NotificationCompat.Builder(this);

                    if (bitmap != null && webSocketEvent.getType() != WebSocketEvent.TYPE_SITE) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            bitmap = getCircleBitmap(bitmap);
                        }

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

    private List<NotificationEvent> parseQmsEvents(WebSocketEvent webSocketEvent) throws Exception {
        NetworkResponse response = Api.getWebClient().get("http://4pda.ru/forum/index.php?act=inspector&CODE=qms");
        List<NotificationEvent> qmsThemes = new ArrayList<>();
        Matcher matcher = inspectorQmsPattern.matcher(response.getBody());
        while (matcher.find()) {
            NotificationEvent notificationEvent = new NotificationEvent(webSocketEvent);

            notificationEvent.setThemeId(Integer.parseInt(matcher.group(1)));
            notificationEvent.setThemeTitle(Utils.fromHtml(matcher.group(2)));
            notificationEvent.setUserId(Integer.parseInt(matcher.group(3)));
            notificationEvent.setUserNick(Utils.fromHtml(matcher.group(4)));
            notificationEvent.setMessageCount(Integer.parseInt(matcher.group(6)));
            if (notificationEvent.getUserNick().isEmpty() && notificationEvent.getThemeId() == 0) {
                notificationEvent.setUserNick("Сообщения 4PDA");
            }
            qmsThemes.add(notificationEvent);
        }
        return qmsThemes;
    }

    private void handleQmsEvent(WebSocketEvent webSocketEvent) {
        Observable.fromCallable(() -> parseQmsEvents(webSocketEvent))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qmsEvents -> {
                    for (NotificationEvent notificationEvent : qmsEvents) {
                        if (notificationEvent.getThemeId() == webSocketEvent.getId()) {
                            sendNotification(notificationEvent);
                        }
                    }
                });
    }

    private List<NotificationEvent> parseFavEvents(WebSocketEvent webSocketEvent) throws Exception {
        NetworkResponse response = Api.getWebClient().get("http://4pda.ru/forum/index.php?act=inspector&CODE=fav");
        List<NotificationEvent> qmsThemes = new ArrayList<>();
        Matcher matcher = inspectorFavoritesPattern.matcher(response.getBody());
        while (matcher.find()) {
            NotificationEvent notificationEvent = new NotificationEvent(webSocketEvent);

            notificationEvent.setThemeId(Integer.parseInt(matcher.group(1)));
            notificationEvent.setThemeTitle(Utils.fromHtml(matcher.group(2)));
            notificationEvent.setMessageCount(Integer.parseInt(matcher.group(3)));
            notificationEvent.setUserId(Integer.parseInt(matcher.group(4)));
            notificationEvent.setUserNick(Utils.fromHtml(matcher.group(5)));
            notificationEvent.setTimeStamp(Integer.parseInt(matcher.group(6)));
            notificationEvent.setReadTimeStamp(Integer.parseInt(matcher.group(7)));
            notificationEvent.setImportant(matcher.group(8).equals("1"));
            qmsThemes.add(notificationEvent);
        }
        return qmsThemes;
    }

    private void handleFavEvent(WebSocketEvent webSocketEvent) {
        Observable.fromCallable(() -> parseFavEvents(webSocketEvent))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(favEvents -> {
                    for (NotificationEvent notificationEvent : favEvents) {
                        if (notificationEvent.getThemeId() == webSocketEvent.getId()) {
                            sendNotification(notificationEvent);
                        }
                    }
                });
    }

    private void handleSiteEvent(WebSocketEvent webSocketEvent) {
        NotificationEvent notificationEvent = new NotificationEvent(webSocketEvent);
        notificationEvent.setThemeId(webSocketEvent.getId());
        sendNotification(notificationEvent);
    }

    private WebSocket webSocket;

    private WebSocketListener webSocketListener = new WebSocketListener() {
        Pattern pattern = Pattern.compile("\\[(\\d+),(\\d+),\"([\\s\\S])(\\d+)\",(\\d+),(\\d+)\\]");
        Matcher matcher = null;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.d("WS_EVENT", "ON OPEN: " + response.toString());
            webSocket.send("[0,\"sv\"]");
            webSocket.send("[0, \"ea\", \"u" + ClientHelper.getUserId() + "\"]");
        }


        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d("WS_EVENT", "ON T MESSAGE: " + text);
            if (matcher == null) {
                matcher = pattern.matcher(text);
            } else {
                matcher = matcher.reset(text);
            }
            if (matcher.find()) {
                WebSocketEvent event = new WebSocketEvent();
                event.setUnknown1(Integer.parseInt(matcher.group(1)));
                event.setUnknown2(Integer.parseInt(matcher.group(2)));
                switch (matcher.group(3)) {
                    case "t":
                        event.setType(WebSocketEvent.TYPE_THEME);
                        break;
                    case "s":
                        event.setType(WebSocketEvent.TYPE_SITE);
                        break;
                    case "q":
                        event.setType(WebSocketEvent.TYPE_QMS);
                        break;
                }
                event.setId(Integer.parseInt(matcher.group(4)));
                switch (Integer.parseInt(matcher.group(5))) {
                    case 1:
                        event.setEventCode(WebSocketEvent.EVENT_NEW);
                        break;
                    case 2:
                        event.setEventCode(WebSocketEvent.EVENT_READ);
                        break;
                    case 3:
                        event.setEventCode(WebSocketEvent.EVENT_MENTION);
                        break;
                    case 4:
                        event.setEventCode(WebSocketEvent.EVENT_HAT_CHANGE);
                        break;
                }
                event.setMessageId(Integer.parseInt(matcher.group(6)));
                Log.e("WS_EVENT", event.toString());
                handleWebSocketEvent(event);
                //sendNotification("Unknown " + event.typeName(), event.codeName() + " in " + event.getThemeId());
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
        }
    };


    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("WS_SERVICE", "Service: onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i("WS_SERVICE", "Service: onRebind");
        super.onRebind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("WS_SERVICE", "Service: onBind");
        return null;
    }

    NotificationManagerCompat mNotificationManager;

    @Override
    public void onCreate() {
        Log.i("WS_SERVICE", "Service: onCreate");
        webSocket = Client.getInstance().createWebSocketConnection(webSocketListener);
        mNotificationManager = NotificationManagerCompat.from(this);
        /*new Handler().postDelayed(() -> {
            webSocketListener.onMessage(webSocket, "[30309,0,\"s344799\",3,3977242]");
        }, 5000);*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("WS_SERVICE", "Service: onStartCommand " + flags + " : " + startId + " : " + intent);
        Log.i("WS_SERVICE", "Service: onStartCommand " + webSocket);
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
        if (webSocket != null)
            webSocket.close(1000, null);
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Intent restartIntent = new Intent(this, getClass());

            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent pi = PendingIntent.getService(this, 1, restartIntent, PendingIntent.FLAG_ONE_SHOT);
            restartIntent.putExtra("RESTART", "RESTART_CHEBUREK");
            am.setExact(AlarmManager.RTC, System.currentTimeMillis() + 3000, pi);
        }
    }

}