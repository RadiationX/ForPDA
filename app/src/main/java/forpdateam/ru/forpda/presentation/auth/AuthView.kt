package forpdateam.ru.forpda.presentation.auth

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel

/**
 * Created by radiationx on 02.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface AuthView : MvpView {
    fun setSendEnabled(isEnabled: Boolean)
    fun setSendRefreshing(isRefreshing: Boolean)
    fun onFormLoaded(authForm: AuthForm)
    fun onSuccessAuth()
    fun showProfile(profile: ProfileModel)
}
