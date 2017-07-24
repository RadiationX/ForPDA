package forpdateam.ru.forpda;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Callable;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.events.models.WebSocketEvent;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.settings.Preferences;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.R.attr.version;

/**
 * Created by radiationx on 23.07.17.
 */

public class SimpleChecker {
    public void checkFromGitHub(Context context) {
        Observable.fromCallable(() -> {
            NetworkResponse response = Client.getInstance().get(CheckerActivity.JSON_LINK);
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

            NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setSmallIcon(R.drawable.ic_notify_mention);

            mBuilder.setContentTitle("Обновление ForPDA");
            mBuilder.setContentText("Новая версия: " + versionName);


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
            if (Preferences.Notifications.Main.isSoundEnabled()) {
                defaults |= NotificationCompat.DEFAULT_SOUND;
            }
            if (Preferences.Notifications.Main.isVibrationEnabled()) {
                defaults |= NotificationCompat.DEFAULT_VIBRATE;
            }
            mBuilder.setDefaults(defaults);

            mNotificationManager.notify(versionCode, mBuilder.build());
        }
    }
}
