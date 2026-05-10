package forpdateam.ru.forpda.extensions

import android.app.PendingIntent
import android.os.Build

fun Int.asMutableFlag(): Int = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> this or PendingIntent.FLAG_MUTABLE
    else -> this
}

fun Int.asImmutableFlag(): Int = this or PendingIntent.FLAG_IMMUTABLE

fun mutableFlag(): Int = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> PendingIntent.FLAG_MUTABLE
    else -> 0
}

fun immutableFlag(): Int = PendingIntent.FLAG_IMMUTABLE