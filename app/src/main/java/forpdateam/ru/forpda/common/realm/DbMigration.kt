package forpdateam.ru.forpda.common.realm

import android.util.Log
import io.realm.DynamicRealm
import io.realm.DynamicRealmObject
import io.realm.RealmMigration
import io.realm.RealmObjectSchema
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by isanechek on 29.08.16.
 *
 *
 * Это хрень нужна для того чтобы мигрировать с одной версии обьекта на другой.
 * Ну вдруг там поля поменялись или еще чего.
 */
class DbMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var updVersion = oldVersion
        val schema = realm.schema

        /*
            for oldest versions
            */
        val oldFavSchema = schema["FavItemBd"]
        if (oldFavSchema != null && !oldFavSchema.hasField("isForum")) {
            oldFavSchema.addField("isForum", Boolean::class.javaPrimitiveType!!)
        }
        val oldHistorySchema = schema["HistoryItemBd"]
        if (oldHistorySchema != null && !oldHistorySchema.hasField("url")) {
            oldHistorySchema.addField("url", String::class.java)
        }

        if (updVersion == 1L) {
            val favSchema = schema["FavItemBd"]
            favSchema?.removeField("isNewMessages")?.removeField("info")
                ?.addField("isNew", Boolean::class.javaPrimitiveType!!)?.addField(
                    "isPoll",
                    Boolean::class.javaPrimitiveType!!
                )?.addField(
                    "isClosed",
                    Boolean::class.javaPrimitiveType!!
                )

            updVersion++
        }

        if (updVersion == 2L) {
            val historySchema = schema["HistoryItemBd"]
            if (historySchema != null) {
                val oldDateFormat = SimpleDateFormat("MM.dd.yy, HH:mm", Locale.getDefault())
                val newDateFormat = SimpleDateFormat("dd.MM.yy, HH:mm", Locale.getDefault())
                historySchema
                    .transform(RealmObjectSchema.Function { dynamicRealmObject: DynamicRealmObject ->
                        val dateString = dynamicRealmObject.getString("date")
                        var date = Date()
                        try {
                            date = requireNotNull(oldDateFormat.parse(dateString))
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }
                        Log.d("SUKA", "DATES " + dateString + " : " + newDateFormat.format(date))
                        dynamicRealmObject.setString("date", newDateFormat.format(date))
                    })
            }

            updVersion++
        }

        if (updVersion == 3L) {
            val favSchema = schema["FavItemBd"]
            favSchema?.addField("curatorId", Int::class.javaPrimitiveType!!)?.addField(
                "curatorNick",
                String::class.java
            )?.addField(
                "subType",
                String::class.java
            )

            updVersion++
        }
    }
}
