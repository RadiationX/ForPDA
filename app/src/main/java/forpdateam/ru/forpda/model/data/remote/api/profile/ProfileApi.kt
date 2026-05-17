package forpdateam.ru.forpda.model.data.remote.api.profile

import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.data.remote.WebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import javax.inject.Inject

/**
 * Created by radiationx on 03.08.16.
 */
class ProfileApi @Inject constructor(
    private val webClient: WebClient,
    private val profileParser: ProfileParser
) {

    suspend fun getProfile(url: String): ProfileModel {
        val response = webClient.get(url)
        return profileParser.parse(response.body, url)
    }

    suspend fun saveNote(note: String): Boolean {
        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=profile-xhr&action=save-note")
            .formHeader("note", note)
        val response = webClient.request(builder.build())
        return response.body == "1"
    }
}
