package forpdateam.ru.forpda.extensions

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun ImageView.setTintColor(@ColorRes colorRes: Int) {
    imageTintList = ColorStateList.valueOf(context.getColor(colorRes))
}

fun ImageView.setTintColorAttr(@AttrRes colorAttr: Int) {
    imageTintList = ColorStateList.valueOf(context.getColorFromAttr(colorAttr))
}
