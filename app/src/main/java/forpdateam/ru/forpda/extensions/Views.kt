package forpdateam.ru.forpda.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun ImageView.setTintColor(@ColorInt colorInt: Int) {
    imageTintList = ColorStateList.valueOf(colorInt)
}

fun ImageView.setTintColorRes(@ColorRes colorRes: Int) {
    imageTintList = ColorStateList.valueOf(context.getColor(colorRes))
}

fun ImageView.setTintColorAttr(@AttrRes colorAttr: Int) {
    imageTintList = ColorStateList.valueOf(context.getColorFromAttr(colorAttr))
}

fun ImageView.clearTint() {
    imageTintList = null
}

fun View.setBackgroundAttr(@AttrRes res: Int) {
    setBackgroundResource(context.getDrawableResAttr(res))
}

fun View.setBackgroundTintColor(@ColorInt colorInt: Int) {
    backgroundTintList = ColorStateList.valueOf(colorInt)
}

fun View.setBackgroundTintColorRes(@ColorRes colorRes: Int) {
    backgroundTintList = ColorStateList.valueOf(context.getColor(colorRes))
}

fun View.setBackgroundTintColorAttr(@AttrRes colorAttr: Int) {
    backgroundTintList = ColorStateList.valueOf(context.getColorFromAttr(colorAttr))
}

fun View.clearBackgroundTint() {
    backgroundTintList = null
}

val ViewBinding.context: Context
    get() = root.context

val RecyclerView.ViewHolder.context: Context
    get() = itemView.context

fun Drawable.transform(block: (Drawable) -> Unit): Drawable {
    val mutated = mutate()
    block(mutated)
    return mutated
}