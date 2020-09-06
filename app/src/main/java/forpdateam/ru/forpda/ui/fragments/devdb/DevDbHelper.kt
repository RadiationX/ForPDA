package forpdateam.ru.forpda.ui.fragments.devdb

import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.SparseArray


object DevDbHelper {
    private val colorFilters = SparseArray<ColorFilter>().apply {
        put(1, PorterDuffColorFilter(Color.parseColor("#850113"), PorterDuff.Mode.SRC_IN))
        put(2, PorterDuffColorFilter(Color.parseColor("#d50000"), PorterDuff.Mode.SRC_IN))
        put(3, PorterDuffColorFilter(Color.parseColor("#ffac00"), PorterDuff.Mode.SRC_IN))
        put(4, PorterDuffColorFilter(Color.parseColor("#99cc00"), PorterDuff.Mode.SRC_IN))
        put(5, PorterDuffColorFilter(Color.parseColor("#339900"), PorterDuff.Mode.SRC_IN))
    }

    fun getColorFilter(rating: Int): ColorFilter {
        return colorFilters.get(getRatingCode(rating))
    }

    fun getRatingCode(rating: Int): Int {
        return Math.max(Math.round(rating / 2.0f), 1)
    }
}