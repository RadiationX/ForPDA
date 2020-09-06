package forpdateam.ru.forpda.entity.remote.theme

import java.util.ArrayList

/**
 * Created by radiationx on 12.11.16.
 */

class PollQuestion {
    var title: String? = null
    val questionItems = mutableListOf<PollQuestionItem>()
}
