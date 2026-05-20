package forpdateam.ru.forpda.model.data.remote.api.profile

import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.data.remote.WebClient
import javax.inject.Inject

/**
 * Created by radiationx on 03.08.16.
 */
class ProfileApi @Inject constructor(
    private val webClient: WebClient,
    private val profileParser: ProfileParser
) {

    suspend fun getProfile(userId: Int): ProfileModel {
        val response = webClient.request(ApiRequest.Forum.Profile.Load(userId))
        return profileParser.parse(response.body, userId)
    }

    suspend fun saveNote(note: String): Boolean {
        val response = webClient.request(ApiRequest.Forum.Profile.SaveNote(note))
        return response.body == "1"
    }
}
