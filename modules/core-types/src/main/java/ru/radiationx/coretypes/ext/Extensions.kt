package ru.radiationx.coretypes.ext

import ru.radiationx.coretypes.PageNumber
import ru.radiationx.coretypes.PageOffset

fun PageNumber?.orDefault(): PageNumber {
    return this ?: PageNumber.default
}

fun PageOffset?.orDefault(): PageOffset {
    return this ?: PageOffset.default
}