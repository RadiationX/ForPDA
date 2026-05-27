package forpdateam.ru.forpda.ui.fragments.devdb.device.di

import forpdateam.ru.forpda.entity.remote.devdb.Device
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class DeviceSharedData @Inject constructor() {
    val deviceFlow = MutableStateFlow<Device?>(null)
}