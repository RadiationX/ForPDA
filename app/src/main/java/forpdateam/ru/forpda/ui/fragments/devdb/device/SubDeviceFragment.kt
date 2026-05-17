package forpdateam.ru.forpda.ui.fragments.devdb.device

import androidx.annotation.LayoutRes
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.extensions.quillMoxyPresenter
import forpdateam.ru.forpda.presentation.devdb.device.SubDevicePresenter
import forpdateam.ru.forpda.presentation.devdb.device.SubDeviceView
import moxy.MvpAppCompatFragment

/**
 * Created by radiationx on 09.08.17.
 */

open class SubDeviceFragment(
    @LayoutRes private val contentLayoutId: Int = 0
) : MvpAppCompatFragment(contentLayoutId), SubDeviceView {
    protected lateinit var device: Device

    protected val presenter by quillMoxyPresenter<SubDevicePresenter>()

    fun setDevice(device: Device): SubDeviceFragment {
        this.device = device
        return this
    }
}
