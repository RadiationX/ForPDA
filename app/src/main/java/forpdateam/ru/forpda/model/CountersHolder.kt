package forpdateam.ru.forpda.model

import android.content.SharedPreferences
import androidx.core.content.edit
import forpdateam.ru.forpda.entity.common.MessageCounters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class CountersHolder @Inject constructor(
    private val preferences: SharedPreferences
) {

    private val dataFlow = MutableStateFlow(load())

    fun observe(): Flow<MessageCounters> = dataFlow

    fun get(): MessageCounters = dataFlow.value

    fun set(value: MessageCounters) {
        preferences
            .edit {
                putInt("counter_qms", value.qms)
                putInt("counter_favorites", value.favorites)
                putInt("counter_mentions", value.mentions)
            }
        dataFlow.value = value
    }

    fun update(block: (MessageCounters) -> MessageCounters) {
        set(block(get()))
    }

    private fun load(): MessageCounters {
        return MessageCounters(
            qms = preferences.getInt("counter_qms", 0),
            favorites = preferences.getInt("counter_favorites", 0),
            mentions = preferences.getInt("counter_mentions", 0)
        )
    }
}