package forpdateam.ru.forpda;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsTheme;
import forpdateam.ru.forpda.api.qms.models.QmsThemes;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.utils.Html;
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

    int nums = 0;
    public void sendNotification(WebSocketEvent event, QmsTheme qmsTheme) {
        String title = qmsTheme.getNick();
        String text = "Диалог \"" + qmsTheme.getName() + "\": " + qmsTheme.getCountNew() + " новых";
        if (notifications.containsKey(event.getId())) {
            NotificationCompat.Builder mBuilder = notifications.get(event.getId());
            mBuilder.setContentTitle(title);
            mBuilder.setContentText(text);
            mNotificationManager.notify(event.getId(), mBuilder.build());

        } else {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

//Create the intent that’ll fire when the user taps the notification//

        /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.androidauthority.com/"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        mBuilder.setContentIntent(pendingIntent);*/



        //Bitmap bitmap = ImageLoader.getInstance().loadImageSync("http://s.4pda.to/JK4K4BZXln6gU7tKqz2b6rtdQ70cwIZGFOYh0BCZ2EnTXbKmm.jpg");

            mBuilder.setSmallIcon(R.drawable.contact_site);
            //mBuilder.setLargeIcon(bitmap);
            mBuilder.setContentTitle(title);
            mBuilder.setContentText(text);
            mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

            mNotificationManager.notify(event.getId(), mBuilder.build());
            notifications.put(event.getId(), mBuilder);
        }


    }

    private void handleWebSocketEvent(WebSocketEvent event) {
        switch (event.type) {
            case WebSocketEvent.TYPE_QMS:
                handleQmsEvent(event);
                break;
            case WebSocketEvent.TYPE_THEME:

                break;
            case WebSocketEvent.TYPE_SITE:

                break;
        }
    }

    private List<QmsTheme> parseThemes() throws Exception {
        NetworkResponse response = Api.getWebClient().get("http://4pda.ru/forum/index.php?act=inspector&CODE=qms");
        List<QmsTheme> qmsThemes = new ArrayList<>();
        Matcher matcher = inspectorQmsPattern.matcher(response.getBody());
        while (matcher.find()) {
            QmsTheme thread = new QmsTheme();
            thread.setId(Integer.parseInt(matcher.group(1)));
            thread.setName(matcher.group(2));
            thread.setUserId(Integer.parseInt(matcher.group(3)));
            thread.setNick(matcher.group(4));
            thread.setCountNew(Integer.parseInt(matcher.group(6)));
            if (thread.getNick().isEmpty() && thread.getId() == 0) {
                thread.setNick("Сообщения 4PDA");
            }
            qmsThemes.add(thread);
        }
        return qmsThemes;
    }

    private void handleQmsEvent(WebSocketEvent event) {
        Observable.fromCallable(this::parseThemes)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qmsThemes -> {
                    for (QmsTheme qmsTheme : qmsThemes) {
                        if (qmsTheme.getId() == event.getId()) {
                            sendNotification(event, qmsTheme);
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