package forpdateam.ru.forpda.realm;

import android.support.annotation.NonNull;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by isanechek on 29.08.16.
 * <p>
 * Это хрень нужна для того чтобы мигрировать с одной версии обьекта на другой.
 * Ну вдруг там поля поменялись или еще чего.
 */

public class DbMigration implements RealmMigration {

    @Override
    public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        /*
            for oldest versions
            */
        RealmObjectSchema oldFavSchema = schema.get("FavItemBd");
        if (oldFavSchema != null && !oldFavSchema.hasField("isForum")) {
            oldFavSchema.addField("isForum", boolean.class);
        }
        RealmObjectSchema oldHistorySchema = schema.get("HistoryItemBd");
        if (oldHistorySchema != null && !oldHistorySchema.hasField("url")) {
            oldHistorySchema.addField("url", String.class);
        }

        if (oldVersion == 1) {
            RealmObjectSchema favSchema = schema.get("FavItemBd");
            if (favSchema != null) {
                favSchema
                        .removeField("isNewMessages")
                        .removeField("info")
                        .addField("isNew", boolean.class)
                        .addField("isPoll", boolean.class)
                        .addField("isClosed", boolean.class);
            }

            oldVersion++;
        }

        if (oldVersion == 2) {
            RealmObjectSchema historySchema = schema.get("HistoryItemBd");
            if (historySchema != null) {
                SimpleDateFormat oldDateFormat = new SimpleDateFormat("MM.dd.yy, HH:mm", Locale.getDefault());
                SimpleDateFormat newDateFormat = new SimpleDateFormat("dd.MM.yy, HH:mm", Locale.getDefault());
                historySchema
                        .transform(dynamicRealmObject -> {
                            String dateString = dynamicRealmObject.getString("date");
                            Date date = new Date();
                            try {
                                date = oldDateFormat.parse(dateString);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Log.d("SUKA", "DATES " + dateString + " : " + newDateFormat.format(date));
                            dynamicRealmObject.setString("date", newDateFormat.format(date));
                        });
            }

            oldVersion++;
        }
    }
}
