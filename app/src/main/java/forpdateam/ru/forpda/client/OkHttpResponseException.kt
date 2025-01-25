package forpdateam.ru.forpda.client

/**
 * Created by RadiationX on 14.08.2016.
 */
class OkHttpResponseException(val code: Int, val name: String, val url: String) : Exception() {
    override val message: String
        get() = "Response {code=$code, message=$name}"
}
