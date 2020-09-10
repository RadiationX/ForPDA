package forpdateam.ru.forpda.ui.fragments.devdb.device

import androidx.fragment.app.Fragment

import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.presentation.devdb.device.DevicePresenter
import forpdateam.ru.forpda.presentation.devdb.device.SubDevicePresenter
import forpdateam.ru.forpda.presentation.devdb.device.SubDeviceView

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
