package forpdateam.ru.forpda.model.data.remote.api

/**
 * Created by radiationx on 07.07.17.
 */
class NetworkResponse(
    val url: String,
    val code: Int,
    val message: String,
    val redirect: String,
    val body: String
) {

    override fun toString(): String {
        return "NetworkResponse{" + code + ", " + message + ", " + url + ", " + redirect + ", " + body.length + "}"
    }
}
