package forpdateam.ru.forpda.entity.remote.devdb

import android.util.Pair

/**
 * Created by radiationx on 06.08.17.
 */

class Device {
    val specs = mutableListOf<Pair<String, List<Pair<String, String>>>>()
    val images = mutableListOf<Pair<String, String>>()
    val comments = mutableListOf<Comment>()
    val discussions = mutableListOf<PostItem>()
    val firmwares = mutableListOf<PostItem>()
    val news = mutableListOf<PostItem>()
    var id: String? = null
    var title: String? = null
    var brandId: String? = null
    var brandTitle: String? = null
    var catId: String? = null
    var catTitle: String? = null
    var rating = 0

    class Comment {
        var id = 0
        var rating = 0
        var userId = 0
        var likes = 0
        var dislikes = 0
        var nick: String? = null
        var date: String? = null
        var text: String? = null
    }

    class PostItem {
        var id = 0
        var image: String? = null
        var title: String? = null
        var date: String? = null
        var desc: String? = null
    }
}
