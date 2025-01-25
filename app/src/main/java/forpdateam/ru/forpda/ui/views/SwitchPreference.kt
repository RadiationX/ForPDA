package forpdateam.ru.forpda.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.preference.SwitchPreferenceCompat

/**
 * Created by radiationx on 26.07.17.
 */
/*
 * Исправляет самопроизвольные переключения настроек в киткате.
 * Пи*дец, да.
 * */
class SwitchPreference : SwitchPreferenceCompat {
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context) : super(context)
}
