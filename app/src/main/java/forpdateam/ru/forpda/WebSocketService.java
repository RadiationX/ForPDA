package forpdateam.ru.forpda;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
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
    private Map<Integer, NotificationCompat.Builder> notifications = new HashMap<>();

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

        /* Theme id: Qms|Site|Fav| */
        private int id = 0;

        /* Code: 1|2|3|4 */
        private int eventCode = 0;

        /*
        * QMS, Mention: message/post id;
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

        @Override
        public String toString() {
            return "WebSocketEvent{" + "unk1=" + unknown1 + ", unk2=" + unknown2 + "; type=" + typeName() + ", code=" + codeName() + ", id=" + id + ", messId=" + messageId + "}";
        }
    }

    public class NotificationEvent {
        private WebSocketEvent webSocketEvent;
        private int id = 0;
        private int userId = 0;
        private int timeStamp = 0;

        private String title = "";
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

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
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

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
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

    public String generateTitle(NotificationEvent notificationEvent) {
        return notificationEvent.getUserNick();
    }

    public String generateContentText(NotificationEvent notificationEvent) {
        WebSocketEvent webSocketEvent = notificationEvent.getWebSocketEvent();
        switch (webSocketEvent.getType()) {
            case WebSocketEvent.TYPE_QMS:
                return notificationEvent.getTitle() + ": " + notificationEvent.getMessageCount() + " непрочитанных сообщений";
            case WebSocketEvent.TYPE_THEME:
                if (webSocketEvent.getEventCode() == WebSocketEvent.EVENT_MENTION) {
                    return "Ответил Вам в теме \"" + notificationEvent.getTitle() + "\"";
                } else {
                    return "Написал сообщение в теме \"" + notificationEvent.getTitle() + "\"";
                }
            case WebSocketEvent.TYPE_SITE:
                return "Ответил на ваш комментарий в \"" + notificationEvent.getTitle() + "\"";
        }
        return "Title";
    }

    public String generateSummaryText(NotificationEvent notificationEvent) {
        WebSocketEvent webSocketEvent = notificationEvent.getWebSocketEvent();
        switch (webSocketEvent.getType()) {
            case WebSocketEvent.TYPE_QMS:
                return "Чат QMS";
            case WebSocketEvent.TYPE_THEME:
                if (webSocketEvent.getEventCode() == WebSocketEvent.EVENT_MENTION) {
                    return "Упоминание";
                } else {
                    return "Избранное";
                }
            case WebSocketEvent.TYPE_SITE:
                return "Комментарий";
        }
        return "Summary";
    }

    public Bitmap getAvatar(NotificationEvent notificationEvent) throws Exception {
        Bitmap bitmap = null;
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
        return bitmap;
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
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
        String title = generateTitle(notificationEvent);
        String text = generateContentText(notificationEvent);
        String summaryText = generateSummaryText(notificationEvent);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(text);
        bigTextStyle.setSummaryText(summaryText);

        WebSocketEvent webSocketEvent = notificationEvent.getWebSocketEvent();
        Observable.fromCallable(() -> getAvatar(notificationEvent))
                .onErrorReturnItem(ImageLoader.getInstance().loadImageSync("assets://av.png"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    Log.d("WS_RX_BITMAP", "" + bitmap);
                    bitmap = getCircleBitmap(bitmap);
                    NotificationCompat.Builder mBuilder;
                    if (notifications.containsKey(webSocketEvent.getId())) {
                        mBuilder = notifications.get(webSocketEvent.getId());
                    } else {
                        mBuilder = new NotificationCompat.Builder(this);
                        mBuilder.setSmallIcon(R.drawable.contact_site);
                        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        notifications.put(webSocketEvent.getId(), mBuilder);
                    }

                    if (bitmap != null) {
                        mBuilder.setLargeIcon(bitmap);
                    }

                    mBuilder.setContentTitle(title);
                    mBuilder.setContentText(text);
                    mBuilder.setStyle(bigTextStyle);


                    if (!notifications.containsKey(webSocketEvent.getId())) {
                        mBuilder.setSmallIcon(R.drawable.contact_site);
                        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        notifications.put(webSocketEvent.getId(), mBuilder);
                    }
                    mNotificationManager.notify(webSocketEvent.getId(), mBuilder.build());
                });
    }

    private void handleWebSocketEvent(WebSocketEvent webSocketEvent) {
        switch (webSocketEvent.type) {
            case WebSocketEvent.TYPE_QMS:
                handleQmsEvent(webSocketEvent);
                break;
            case WebSocketEvent.TYPE_THEME:
                handleFavEvent(webSocketEvent);
                break;
            case WebSocketEvent.TYPE_SITE:

                break;
        }
    }

    private List<NotificationEvent> parseQmsThemes(WebSocketEvent webSocketEvent) throws Exception {
        NetworkResponse response = Api.getWebClient().get("http://4pda.ru/forum/index.php?act=inspector&CODE=qms");
        List<NotificationEvent> qmsThemes = new ArrayList<>();
        Matcher matcher = inspectorQmsPattern.matcher(response.getBody());
        while (matcher.find()) {
            NotificationEvent notificationEvent = new NotificationEvent(webSocketEvent);

            notificationEvent.setId(Integer.parseInt(matcher.group(1)));
            notificationEvent.setTitle(Utils.htmlEncode(matcher.group(2)));
            notificationEvent.setUserId(Integer.parseInt(matcher.group(3)));
            notificationEvent.setUserNick(Utils.htmlEncode(matcher.group(4)));
            notificationEvent.setMessageCount(Integer.parseInt(matcher.group(6)));
            if (notificationEvent.getUserNick().isEmpty() && notificationEvent.getId() == 0) {
                notificationEvent.setUserNick("Сообщения 4PDA");
            }
            qmsThemes.add(notificationEvent);
        }
        return qmsThemes;
    }

    private void handleQmsEvent(WebSocketEvent webSocketEvent) {
        Observable.fromCallable(() -> parseQmsThemes(webSocketEvent))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qmsThemes -> {
                    for (NotificationEvent notificationEvent : qmsThemes) {
                        if (notificationEvent.getId() == webSocketEvent.getId()) {
                            sendNotification(notificationEvent);
                        }
                    }
                });
    }

    private List<NotificationEvent> parseFavThemes(WebSocketEvent webSocketEvent) throws Exception {
        NetworkResponse response = Api.getWebClient().get("http://4pda.ru/forum/index.php?act=inspector&CODE=fav");
        List<NotificationEvent> qmsThemes = new ArrayList<>();
        Matcher matcher = inspectorFavoritesPattern.matcher(response.getBody());
        while (matcher.find()) {
            NotificationEvent notificationEvent = new NotificationEvent(webSocketEvent);

            notificationEvent.setId(Integer.parseInt(matcher.group(1)));
            notificationEvent.setTitle(Utils.htmlEncode(matcher.group(2)));
            notificationEvent.setMessageCount(Integer.parseInt(matcher.group(3)));
            notificationEvent.setUserId(Integer.parseInt(matcher.group(4)));
            notificationEvent.setUserNick(Utils.htmlEncode(matcher.group(5)));
            notificationEvent.setTimeStamp(Integer.parseInt(matcher.group(6)));
            notificationEvent.setReadTimeStamp(Integer.parseInt(matcher.group(7)));
            notificationEvent.setImportant(matcher.group(8).equals("1"));
            qmsThemes.add(notificationEvent);
        }
        return qmsThemes;
    }

    private void handleFavEvent(WebSocketEvent webSocketEvent) {
        Observable.fromCallable(() -> parseFavThemes(webSocketEvent))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qmsThemes -> {
                    for (NotificationEvent notificationEvent : qmsThemes) {
                        if (notificationEvent.getId() == webSocketEvent.getId()) {
                            sendNotification(notificationEvent);
                        }
                    }
                });
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
                //sendNotification("Unknown " + event.typeName(), event.codeName() + " in " + event.getId());
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
    public IBinder onBind(Intent intent) {
        return null;
    }

    NotificationManagerCompat mNotificationManager;

    @Override
    public void onCreate() {
        Log.i("WS_SERVICE", "Service: onCreate");
        webSocket = Client.getInstance().createWebSocketConnection(webSocketListener);
        mNotificationManager = NotificationManagerCompat.from(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("WS_SERVICE", "Service: onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("WS_SERVICE", "Service: onDestroy");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("WS_SERVICE", "Service: onTaskRemoved");
        if (Build.VERSION.SDK_INT == 19) {
            Intent restartIntent = new Intent(this, getClass());

            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent pi = PendingIntent.getService(this, 1, restartIntent, PendingIntent.FLAG_ONE_SHOT);
            restartIntent.putExtra("RESTART", "RESTART_CHEBUREK");
            am.setExact(AlarmManager.RTC, System.currentTimeMillis() + 3000, pi);
        }
    }

}