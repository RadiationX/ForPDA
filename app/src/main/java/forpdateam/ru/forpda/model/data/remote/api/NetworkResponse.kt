package forpdateam.ru.forpda.model.data.remote.api

/**
 * Created by radiationx on 07.07.17.
 */
class NetworkResponse(url: String) {
    var code: Int = 0
    var message: String = ""
    var url: String = ""
    var redirect: String = url
    @JvmField
    var body: String = ""

    init {
        this.url = url
    }

    override fun toString(): String {
        return "NetworkResponse{" + code + ", " + message + ", " + url + ", " + redirect + ", " + body.length + "}"
    }
}
