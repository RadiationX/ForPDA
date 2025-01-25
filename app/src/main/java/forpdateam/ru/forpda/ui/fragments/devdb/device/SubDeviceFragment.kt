package forpdateam.ru.forpda.ui.fragments.devdb.device

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.presentation.devdb.device.SubDevicePresenter
import forpdateam.ru.forpda.presentation.devdb.device.SubDeviceView
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * Created by radiationx on 09.08.17.
 */

open class SubDeviceFragment : MvpAppCompatFragment(), SubDeviceView {
    protected lateinit var device: Device

    @InjectPresenter
    lateinit var presenter: SubDevicePresenter

    @ProvidePresenter
    internal fun providePresenter(): SubDevicePresenter = SubDevicePresenter(
        App.get().Di().router,
        App.get().Di().linkHandler
    )

    fun setDevice(device: Device): SubDeviceFragment {
        this.device = device
        return this
    }
}
