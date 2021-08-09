package forpdateam.ru.forpda.model.data.remote.api.profile

import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest

/**
 * Created by radiationx on 03.08.16.
 */
class ProfileApi(
        private val webClient: IWebClient,
        private val profileParser: ProfileParser
) {

    fun getProfile(url: String): ProfileModel {
        val response = webClient.get(url)
        return profileParser.parse(response.body, url)
    }

    fun saveNote(note: String): Boolean {
        val builder = NetworkRequest.Builder()
                .url("https://4pda.to/forum/index.php?act=profile-xhr&action=save-note")
                .formHeader("note", note)
        val response = webClient.request(builder.build())
        return response.body == "1"
    }
}
