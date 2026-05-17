package forpdateam.ru.forpda.model.data.remote.api

import forpdateam.ru.forpda.model.data.remote.WebClient

/**
 * Created by radiationx on 02.05.17.
 */
class NetworkRequest(
    val url: String,
    val headers: Map<String, String>,
    val formHeaders: Map<String, String>,
    val encodedFormHeaders: Set<String>,
    val isMultipartForm: Boolean,
    val file: File?,
    val isWithoutBody: Boolean
) {

    class Builder {
        private var url: String = ""
        private var headers: MutableMap<String, String>? = null
        private var formHeaders: MutableMap<String, String>? = null
        private var encodedFormHeaders: MutableSet<String>? = null
        private var isMultipartForm: Boolean = false
        private var file: File? = null
        private var withoutBody: Boolean = false

        private fun getHeaders(): MutableMap<String, String> {
            return (headers ?: mutableMapOf()).also { headers = it }
        }

        private fun getFormHeaders(): MutableMap<String, String> {
            return (formHeaders ?: mutableMapOf()).also { formHeaders = it }
        }

        private fun getEncodedFormHeaders(): MutableSet<String> {
            return (encodedFormHeaders ?: mutableSetOf()).also { encodedFormHeaders = it }
        }

        fun url(url: String): Builder {
            this.url = url
            return this
        }

        fun addHeaders(headers: Map<String, String>): Builder {
            getHeaders().putAll(headers)
            return this
        }

        fun addHeader(name: String, value: String): Builder {
            getHeaders()[name] = value
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
            getFormHeaders().putAll(formHeaders)
            if (encoded) {
                getEncodedFormHeaders().addAll(formHeaders.keys)
            }
            return this
        }

        fun formHeader(name: String, value: String): Builder {
            return formHeader(name, value, false)
        }

        fun formHeader(name: String, value: String, encoded: Boolean): Builder {
            getFormHeaders()[name] = value
            if (encoded) {
                getEncodedFormHeaders().add(name)
            }
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

        fun file(file: File?): Builder {
            this.file = file
            isMultipartForm = true
            return this
        }

        fun build(): NetworkRequest {
            return NetworkRequest(
                url = url,
                headers = headers?.toMap().orEmpty(),
                formHeaders = formHeaders?.toMap().orEmpty(),
                encodedFormHeaders = encodedFormHeaders?.toSet().orEmpty(),
                isMultipartForm = isMultipartForm,
                file = file,
                isWithoutBody = withoutBody,
            )
        }
    }

    data class File(
        val requestName: String,
        val file: RequestFile,
        val progressListener: WebClient.ProgressListener
    )
}
