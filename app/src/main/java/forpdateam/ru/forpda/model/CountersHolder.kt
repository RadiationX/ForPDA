package forpdateam.ru.forpda.model

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.entity.common.MessageCounters
import io.reactivex.Observable

class CountersHolder(
        private val preferences: SharedPreferences,
        private val schedulers: SchedulersProvider
) {
    private val relay = BehaviorRelay.create<MessageCounters>()

    init {
        set(MessageCounters().apply {
            qms = preferences.getInt("counter_qms", 0)
            favorites = preferences.getInt("counter_favorites", 0)
            mentions = preferences.getInt("counter_mentions", 0)
        })
    }

    fun observe(): Observable<MessageCounters> = relay
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui());

    fun get(): MessageCounters = relay.value!!

    fun set(value: MessageCounters) {
        preferences
                .edit()
                .putInt("counter_qms", value.qms)
                .putInt("counter_favorites", value.favorites)
                .putInt("counter_mentions", value.mentions)
                .apply()
        relay.accept(value)
    }
}