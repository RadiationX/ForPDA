package forpdateam.ru.forpda.extensions

fun <T> MutableList<T>.replace(condition: (T) -> Boolean, map: (T) -> T) {
    val index = indexOfFirst(condition)
    if (index == -1) return
    val oldItem = get(index)
    val newItem = map.invoke(oldItem)
    set(index, newItem)
}