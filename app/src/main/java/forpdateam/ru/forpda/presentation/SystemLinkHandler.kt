package forpdateam.ru.forpda.presentation

interface SystemLinkHandler {
    fun handle(url: String)
    fun handleDownload(url: String)
}