package forpdateam.ru.forpda.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDimensionPixelSizeOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.use

@DrawableRes
fun Context.getDrawableResAttr(@AttrRes attr: Int): Int {
    return obtainStyledAttributes(intArrayOf(attr)).use {
        it.getResourceIdOrThrow(0)
    }
}

fun Context.getDrawableAttr(@AttrRes attr: Int): Drawable? {
    return getDrawable(getDrawableResAttr(attr))
}

fun Context.getDimensionPixelSizeAttr(@AttrRes attr: Int): Int {
    return obtainStyledAttributes(intArrayOf(attr)).use {
        it.getDimensionPixelSizeOrThrow(0)
    }
}

@ColorInt
fun Context.getColorFromAttr(@AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    return obtainStyledAttributes(intArrayOf(attr)).use {
        it.getColorOrThrow(0)
    }
}

fun Context.dpToPx(dp: Int): Int = (this.resources.displayMetrics.density * dp).toInt()
