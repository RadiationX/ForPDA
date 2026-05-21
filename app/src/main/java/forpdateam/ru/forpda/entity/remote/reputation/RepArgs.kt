package forpdateam.ru.forpda.entity.remote.reputation

import android.os.Parcelable
import forpdateam.ru.forpda.extensions.mapOnce
import forpdateam.ru.forpda.extensions.requireOnce
import kotlinx.parcelize.Parcelize
import java.util.regex.Pattern

/**
 * Created by radiationx on 20.03.17.
 */
@Parcelize
data class RepArgs(
    val userId: Int,
    val initialSt: Int,
    val mode: String,
    val sort: String
) : Parcelable {

    companion object {
        const val MODE_TO = "to"
        const val MODE_FROM = "from"
        const val SORT_ASC = "asc"
        const val SORT_DESC = "desc"

        fun fromUrl(url: String): RepArgs {
            return RepArgs(
                userId = Pattern.compile("mid=(\\d+)").matcher(url).requireOnce {
                    it.group(1).toInt()
                },
                initialSt = Pattern.compile("st=(\\d+)").matcher(url).mapOnce {
                    it.group(1).toInt()
                } ?: 0,
                mode = Pattern.compile("mode=([^&]+)").matcher(url).mapOnce {
                    when (it.group(1)) {
                        MODE_FROM -> MODE_FROM
                        MODE_TO -> MODE_TO
                        else -> null
                    }
                } ?: MODE_TO,
                sort = Pattern.compile("order=([^&]+)").matcher(url).mapOnce {
                    when (it.group(1)) {
                        SORT_ASC -> SORT_ASC
                        SORT_DESC -> SORT_DESC
                        else -> null
                    }
                } ?: SORT_DESC
            )
        }
    }
}
