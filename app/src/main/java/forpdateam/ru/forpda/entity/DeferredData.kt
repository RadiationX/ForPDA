package forpdateam.ru.forpda.entity

data class DeferredData<T>(val value: T)

fun <T> T.asDeferredData(): DeferredData<T> {
    return DeferredData(this)
}