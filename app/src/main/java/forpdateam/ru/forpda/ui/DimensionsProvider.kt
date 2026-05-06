package forpdateam.ru.forpda.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Created by radiationx on 09.01.18.
 */
class DimensionsProvider {
    private val dimensionsState = MutableStateFlow(DimensionHelper.Dimensions())
    fun observeDimensions(): Flow<DimensionHelper.Dimensions> = dimensionsState
    fun getDimensions(): DimensionHelper.Dimensions = dimensionsState.value
    fun update(dimensions: DimensionHelper.Dimensions) {
        dimensionsState.value = dimensions
    }
}