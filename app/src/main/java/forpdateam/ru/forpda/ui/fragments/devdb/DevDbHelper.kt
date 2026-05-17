package forpdateam.ru.forpda.ui.fragments.devdb

import android.util.SparseIntArray
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt


object DevDbHelper {

    private val ratingColors = SparseIntArray().apply {
        put(1, "#850113".toColorInt())
        put(2, "#d50000".toColorInt())
        put(3, "#ffac00".toColorInt())
        put(4, "#99cc00".toColorInt())
        put(5, "#339900".toColorInt())
    }

    @ColorInt
    fun getColor(rating: Int): Int {
        return ratingColors.get(getRatingCode(rating))
    }

    fun getRatingCode(rating: Int): Int {
        return Math.max(Math.round(rating / 2.0f), 1)
    }
}