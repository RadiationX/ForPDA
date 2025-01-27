package forpdateam.ru.forpda.entity.remote.devdb

/**
 * Created by radiationx on 06.08.17.
 */

data class Device(
    val id: String,
    val title: String,
    val brandId: String,
    val brandTitle: String,
    val catId: String,
    val catTitle: String,
    val rating: Int,
    val specs: List<Pair<String, List<Pair<String, String>>>>,
    val images: List<Pair<String, String>>,
    val comments: List<Comment>,
    val discussions: List<PostItem>,
    val firmwares: List<PostItem>,
    val news: List<PostItem>,
) {

    data class Comment(
        val id: Int,
        val rating: Int,
        val userId: Int,
        val likes: Int,
        val dislikes: Int,
        val nick: String,
        val date: String,
        val text: String
    )

    data class PostItem(
        val id: Int,
        val image: String?,
        val title: String,
        val date: String,
        val desc: String?,
    )
}
