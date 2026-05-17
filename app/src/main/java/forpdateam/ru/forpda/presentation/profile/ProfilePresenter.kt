package forpdateam.ru.forpda.presentation.profile

import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moxy.InjectViewState

/**
 * Created by radiationx on 02.01.18.
 */

@InjectViewState
class ProfilePresenter(
    private val profileRepository: ProfileRepository,
    private val router: TabRouter,
    private val linkHandler: ILinkHandler,
    private val errorHandler: IErrorHandler,
    private val utils: Utils
) : BasePresenter<ProfileView>() {

    var profileUrl: String? = null
    private var currentData: ProfileModel? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadProfile()
    }

    private fun loadProfile() {
        val url = profileUrl ?: return
        viewState.setRefreshing(true)
        viewModelScope.launch {
            coRunCatching {
                profileRepository.loadProfile(url)
            }.onSuccess { profileModel ->
                currentData = profileModel
                loadAvatar(profileModel)
                viewState.showProfile(profileModel)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
        viewState.setRefreshing(false)
    }

    fun saveNote(note: String) {
        viewModelScope.launch {
            coRunCatching {
                profileRepository.saveNote(note)
            }.onSuccess {
                viewState.onSaveNote(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
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
        utils.copyToClipBoard(profileUrl)
    }

    fun navigateToQms() {
        currentData?.let {
            linkHandler.handle(it.contacts[0].url, router)
        }
    }

    private fun loadAvatar(profile: ProfileModel) {
        viewModelScope.launch {
            coRunCatching {
                withContext(Dispatchers.IO){
                    ImageLoader.getInstance().loadImageSync(profile.user.avatar)
                }
            }.onSuccess {
                viewState.showAvatar(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

}
