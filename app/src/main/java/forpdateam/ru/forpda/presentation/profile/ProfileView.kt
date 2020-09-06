package forpdateam.ru.forpda.presentation.profile

import android.graphics.Bitmap
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel

/**
 * Created by radiationx on 02.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface ProfileView : IBaseView {
    fun showProfile(data: ProfileModel)

    fun showAvatar(bitmap: Bitmap)

    @StateStrategyType(SkipStrategy::class)
    fun onSaveNote(success: Boolean)
}
