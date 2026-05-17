package forpdateam.ru.forpda.model.data.remote.api.checker

import forpdateam.ru.forpda.entity.app.checker.UpdateData
import forpdateam.ru.forpda.model.data.remote.WebClient
import javax.inject.Inject

/**
 * Created by radiationx on 28.01.18.
 */
class CheckerApi @Inject constructor(
    private val client: WebClient,
    private val checkerParser: CheckerParser,
) {

    suspend fun checkUpdate(): UpdateData = client
        .get("https://bitbucket.org/RadiationX/apps-updates/raw/master/forpda/check.json")
        .let { checkerParser.parse(it.body) }

}