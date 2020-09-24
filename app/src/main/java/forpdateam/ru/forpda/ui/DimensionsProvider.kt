package forpdateam.ru.forpda.ui

import androidx.annotation.Dimension
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

/**
 * Created by radiationx on 09.01.18.
 */
class DimensionsProvider {
    private val relay = BehaviorRelay.createDefault(DimensionHelper.Dimensions())
    fun observeDimensions(): Observable<DimensionHelper.Dimensions> = relay
    fun getDimensions(): DimensionHelper.Dimensions = relay.value!!
    fun update(dimensions: DimensionHelper.Dimensions) {
        relay.accept(dimensions)
    }
}