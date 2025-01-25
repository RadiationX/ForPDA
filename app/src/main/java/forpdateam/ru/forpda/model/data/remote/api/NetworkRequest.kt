package forpdateam.ru.forpda.model.data.remote.api

/**
 * Created by radiationx on 02.05.17.
 */
class NetworkRequest(builder: Builder) {
    var url: String = ""
    val headers: LinkedHashMap<String, String>?
    val formHeaders: LinkedHashMap<String, String>?
    val encodedFormHeaders: Set<String>?
    var isMultipartForm: Boolean = false
    var file: RequestFile? = null

    //true - get, false - post
    var method: Boolean = true
    var isWithoutBody: Boolean = false

    init {
        this.url = builder.url
        this.headers = builder.headers
        this.formHeaders = builder.formHeaders
        this.encodedFormHeaders = builder.encodedFormHeaders
        this.isMultipartForm = builder.isMultipartForm
        this.file = builder.file
        this.method = builder.method
        this.isWithoutBody = builder.withoutBody
    }

    class Builder {
        internal var url: String = ""
        var headers: LinkedHashMap<String, String>? = null
        var formHeaders: LinkedHashMap<String, String>? = null
        var encodedFormHeaders: MutableSet<String>? = null
        var isMultipartForm: Boolean = false
        var file: RequestFile? = null
        var method: Boolean = true
        var withoutBody: Boolean = false

        fun url(url: String): Builder {
            this.url = url
            return this
        }

        fun addHeaders(headers: LinkedHashMap<String, String>): Builder {
            if (this.headers == null) this.headers = LinkedHashMap()
            this.headers!!.putAll(headers)
            return this
        }

        fun addHeader(name: String, value: String): Builder {
            if (this.headers == null) this.headers = LinkedHashMap()
            headers!![name] = value
            return this
        }

        fun xhrHeader(): Builder {
            addHeader("X-Requested-With", "XMLHttpRequest")
            return this
        }

        fun formHeaders(formHeaders: Map<String, String>): Builder {
            return formHeaders(formHeaders, false)
        }

        fun formHeaders(formHeaders: Map<String, String>, encoded: Boolean): Builder {
            if (this.formHeaders == null) this.formHeaders = LinkedHashMap()
            this.formHeaders!!.putAll(formHeaders)
            if (encoded) {
                if (this.encodedFormHeaders == null) {
                    encodedFormHeaders = HashSet()
                }
                encodedFormHeaders!!.addAll(this.formHeaders!!.keys)
            }
            method = false
            return this
        }

        fun formHeader(name: String, value: String): Builder {
            return formHeader(name, value, false)
        }

        fun formHeader(name: String, value: String, encoded: Boolean): Builder {
            if (this.formHeaders == null) this.formHeaders = LinkedHashMap()
            formHeaders!![name] = value
            if (encoded) {
                if (this.encodedFormHeaders == null) {
                    encodedFormHeaders = HashSet()
                }
                encodedFormHeaders!!.add(name)
            }
            method = false
            return this
        }

        fun multipart(): Builder {
            isMultipartForm = true
            return this
        }

        fun withoutBody(): Builder {
            withoutBody = true
            return this
        }

        fun file(file: RequestFile?): Builder {
            this.file = file
            isMultipartForm = true
            method = false
            return this
        }

        fun build(): NetworkRequest {
            return NetworkRequest(this)
        }
    }
}
