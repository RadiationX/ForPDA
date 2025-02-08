package forpdateam.ru.forpda.ui.fragments.devdb.device.specs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.DeviceFragmentSpecsBinding
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment
import forpdateam.ru.forpda.ui.fragments.devdb.device.SubDeviceFragment

/**
 * Created by radiationx on 08.08.17.
 */

class SpecsFragment : SubDeviceFragment(R.layout.device_fragment_specs) {

    private val binding by viewBinding<DeviceFragmentSpecsBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.baseList.layoutManager = LinearLayoutManager(binding.baseList.context)
        val adapter = SpecsAdapter()
        adapter.addAll(device.specs)
        binding.baseList.adapter = adapter
        binding.baseList.addItemDecoration(DevicesFragment.SpacingItemDecoration(App.px8, true))
    }
}
