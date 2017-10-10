package forpdateam.ru.forpda.realm;

import android.support.annotation.NonNull;

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

        if (oldVersion == 1) {
            RealmObjectSchema schemaNews = schema.get("FavItemBd");
            if (schemaNews != null) {
                schemaNews
                        .removeField("isNewMessages")
                        .removeField("info")
                        .addField("isNew", boolean.class)
                        .addField("isPoll", boolean.class)
                        .addField("isClosed", boolean.class);
            }

            oldVersion++;
        }
    }
}
