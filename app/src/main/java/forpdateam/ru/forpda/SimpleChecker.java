package forpdateam.ru.forpda;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import org.json.JSONException;
import org.json.JSONObject;

import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.settings.Preferences;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 23.07.17.
 */

public class SimpleChecker {
    public void checkFromGitHub(Context context) {
        Observable.fromCallable(() -> {
            NetworkResponse response = Client.get().get(CheckerActivity.JSON_LINK);
            return response.getBody();
        })
                .onErrorReturn(throwable -> "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> checkSource(s, context));
    }

    private void checkSource(String jsonSource, Context context) {
        if (jsonSource.length() == 0) {
            return;
        }
        try {
            final JSONObject jsonBody = new JSONObject(jsonSource);
            final JSONObject update = jsonBody.getJSONObject("update");
            checkUpdate(update, context, jsonSource);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkUpdate(JSONObject updateObject, Context context, String jsonSource) throws JSONException {
        if (context == null) {
            context = App.getContext();
        }
        final int currentVersionCode = BuildConfig.VERSION_CODE;
        final int versionCode = Integer.parseInt(updateObject.getString("version_code"));

        if (versionCode > currentVersionCode) {
            final String versionName = updateObject.getString("version_name");


            String channelId = "forpda_channel_updates";
            String channelName = context.getString(R.string.updater_notification_title);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                NotificationManager manager = context.getSystemService(NotificationManager.class);
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                }
            }


            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId);

            NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);

            mBuilder.setSmallIcon(R.drawable.ic_notify_mention);

            mBuilder.setContentTitle(context.getString(R.string.updater_notification_title));
            mBuilder.setContentText(String.format(context.getString(R.string.updater_notification_content_VerName), versionName));
            mBuilder.setChannelId(channelId);


            Intent notifyIntent = new Intent(context, CheckerActivity.class);
            //notifyIntent.setData(Uri.parse(createIntentUrl(notificationEvent)));
            notifyIntent.putExtra(CheckerActivity.JSON_SOURCE, jsonSource);
            notifyIntent.setAction(Intent.ACTION_VIEW);
            PendingIntent notifyPendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, 0);
            mBuilder.setContentIntent(notifyPendingIntent);

            mBuilder.setAutoCancel(true);

            mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            mBuilder.setCategory(NotificationCompat.CATEGORY_EVENT);


            int defaults = 0;
            /*if (Preferences.Notifications.Main.isSoundEnabled()) {
                defaults |= NotificationCompat.DEFAULT_SOUND;
            }*/
            if (Preferences.Notifications.Main.isVibrationEnabled()) {
                defaults |= NotificationCompat.DEFAULT_VIBRATE;
            }
            mBuilder.setDefaults(defaults);

            mNotificationManager.notify(versionCode, mBuilder.build());
        }
    }
}
