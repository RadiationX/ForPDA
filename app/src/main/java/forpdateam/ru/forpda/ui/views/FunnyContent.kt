package forpdateam.ru.forpda.ui.views

import android.content.Context
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.databinding.FunnyContentBinding

/**
 * Created by radiationx on 06.10.17.
 */
class FunnyContent(context: Context?) : RelativeLayout(context) {

    private val binding by viewBinding<FunnyContentBinding>(attachToRoot = true)

    fun setImage(@DrawableRes resId: Int): FunnyContent {
        binding.funnyImage.setImageResource(resId)
        return this
    }

    fun setTitle(@StringRes resId: Int): FunnyContent {
        binding.funnyTitle.setText(resId)
        binding.funnyTitle.visibility = VISIBLE
        return this
    }

    fun setDesc(@StringRes resId: Int): FunnyContent {
        binding.funnyDesc.setText(resId)
        binding.funnyDesc.visibility = VISIBLE
        return this
    }
}
