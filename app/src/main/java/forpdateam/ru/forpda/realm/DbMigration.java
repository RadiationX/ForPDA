package forpdateam.ru.forpda.realm;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by isanechek on 29.08.16.
 *
 * Это хрень нужна для того чтобы мигрировать с одной версии обьекта на другой.
 * Ну вдруг там поля поменялись или еще чего.
 *
 */

public class DbMigration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();

        /*------------------------------NEWS MIGRATION SCHEMA------------------------------*/
        /*Версия 2 это только для примера*/
        if (oldVersion == 2) {
            RealmObjectSchema schemaNews = schema.get("NewsModel");// Тут какой класс(таблицу) будем ломать
            schemaNews
                    .addField("insetTime", long.class, FieldAttribute.REQUIRED)
                    .transform(obj -> obj.setLong("insertTime", 0L)); // Тут мы добавляем long поле
        }
        /*------------------------------NEWS MIGRATION END------------------------------*/


        /*
        * Всегда должно быть в жопе мира.
        * Хотя и особой разницы нет :D
         */
        oldVersion++;
    }
}
