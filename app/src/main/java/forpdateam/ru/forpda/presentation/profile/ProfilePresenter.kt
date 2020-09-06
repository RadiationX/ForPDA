package forpdateam.ru.forpda.presentation.profile

import com.arellomobile.mvp.InjectViewState
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.TabRouter
import io.reactivex.Single

/**
 * Created by radiationx on 02.01.18.
 */

@InjectViewState
class ProfilePresenter(
        private val profileRepository: ProfileRepository,
        private val router: TabRouter,
        private val linkHandler: ILinkHandler,
        private val errorHandler: IErrorHandler,
        private val schedulers: SchedulersProvider
) : BasePresenter<ProfileView>() {

    var profileUrl: String? = null
    private var currentData: ProfileModel? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadProfile()
    }

    private fun loadProfile() {
        profileUrl?.let {
            profileRepository
                    .loadProfile(it)
                    .doOnSubscribe { viewState.setRefreshing(true) }
                    .doAfterTerminate { viewState.setRefreshing(false) }
                    .subscribe({ profileModel ->
                        currentData = profileModel
                        loadAvatar(profileModel)
                        viewState.showProfile(profileModel)
                    }, {
                        errorHandler.handle(it)
                    })
                    .untilDestroy()
        }
    }

    fun saveNote(note: String) {
        profileRepository
                .saveNote(note)
                .subscribe({
                    viewState.onSaveNote(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun onContactClick(item: ProfileModel.Contact) {
        linkHandler.handle(item.url, router)
    }

    fun onDeviceClick(item: ProfileModel.Device) {
        linkHandler.handle(item.url, router)
    }

    fun onStatClick(item: ProfileModel.Stat) {
        linkHandler.handle(item.url, router)
    }

    fun copyUrl() {
        Utils.copyToClipBoard(profileUrl)
    }

    fun navigateToQms() {
        currentData?.let {
            linkHandler.handle(it.contacts[0].url, router)
        }
    }

    private fun loadAvatar(profile: ProfileModel) {
        Single
                .fromCallable {
                    ImageLoader.getInstance().loadImageSync(profile.avatar)
                }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe({
                    viewState.showAvatar(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

}
