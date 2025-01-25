package forpdateam.ru.forpda.presentation.auth

import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

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
