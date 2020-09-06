package forpdateam.ru.forpda.entity.remote.devdb

import android.util.Pair

import java.util.ArrayList

/**
 * Created by radiationx on 06.08.17.
 */

open class Brand {
    val devices = mutableListOf<DeviceItem>()
    var id: String? = null
    var title: String? = null
    var catId: String? = null
    var catTitle: String? = null
    var actual = 0
    var all = 0

    class DeviceItem {
        val specs = mutableListOf<Pair<String, String>>()
        var id: String? = null
        var title: String? = null
        var price: String? = null
        var imageSrc: String? = null
        var rating = 0
    }
}
