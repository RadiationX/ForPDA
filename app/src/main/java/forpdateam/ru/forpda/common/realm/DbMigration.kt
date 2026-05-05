package forpdateam.ru.forpda.common.realm

import io.realm.kotlin.dynamic.getNullableValue
import io.realm.kotlin.migration.AutomaticSchemaMigration
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
class DbMigration : AutomaticSchemaMigration {

    override fun migrate(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        var updVersion = migrationContext.oldRealm.version().version

        migrateOldest(migrationContext)

        if (updVersion == 1L) {
            migrateV2(migrationContext)
            updVersion++
        }

        if (updVersion == 2L) {
            migrateV3(migrationContext)
            updVersion++
        }

        if (updVersion == 3L) {
            migrateV4(migrationContext)
            updVersion++
        }
    }

    private fun migrateOldest(context: AutomaticSchemaMigration.MigrationContext) {
        context.enumerate("FavItemBd") { old, new ->
            if (old.getObject("isForum") == null) {
                new?.set("isForum", false)
            }
        }
        context.enumerate("HistoryItemBd") { old, new ->
            if (old.getObject("url") == null) {
                new?.set("url", false)
            }
        }
    }

    private fun migrateV2(context: AutomaticSchemaMigration.MigrationContext) {
        context.enumerate("FavItemBd") { old, new ->
            // removed isNewMessages
            // removed info
            new?.set("isNew", false)
            new?.set("isPoll", false)
            new?.set("isClosed", false)
        }
    }

    private fun migrateV3(context: AutomaticSchemaMigration.MigrationContext) {
        val oldDateFormat = SimpleDateFormat("MM.dd.yy, HH:mm", Locale.getDefault())
        val newDateFormat = SimpleDateFormat("dd.MM.yy, HH:mm", Locale.getDefault())
        context.enumerate("HistoryItemBd") { old, new ->
            val newDate = old.getNullableValue<String>("date")?.also {
                var date = Date()
                try {
                    date = requireNotNull(oldDateFormat.parse(it))
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                newDateFormat.format(date)
            }
            new?.set("date", newDate)
        }
    }

    private fun migrateV4(context: AutomaticSchemaMigration.MigrationContext) {
        context.enumerate("FavItemBd") { old, new ->
            val nullString: String? = null
            new?.set("curatorId", 0)
            new?.set("curatorNick", nullString)
            new?.set("subType", nullString)
        }
    }
}
