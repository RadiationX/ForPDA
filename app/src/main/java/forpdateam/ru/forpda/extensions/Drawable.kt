package forpdateam.ru.forpda.extensions

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt


fun Drawable.transform(block: (Drawable) -> Unit): Drawable {
    val mutated = mutate()
    block(mutated)
    return mutated
}

fun Drawable.mutateWithTint(@ColorInt colorInt: Int?): Drawable {
    return transform {
        if (colorInt != null) {
            setTint(colorInt)
        } else {
            setTintList(null)
        }
    }
}