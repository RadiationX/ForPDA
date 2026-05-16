package forpdateam.ru.forpda.ui.fragments.devdb.device.posts

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.DeviceFragmentSpecsBinding
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.extensions.getDimenPx
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment
import forpdateam.ru.forpda.ui.fragments.devdb.device.SubDeviceFragment

/**
 * Created by radiationx on 09.08.17.
 */

class PostsFragment : SubDeviceFragment(R.layout.device_fragment_specs) {

    private val binding by viewBinding<DeviceFragmentSpecsBinding>()

    private var source = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.baseList.layoutManager = LinearLayoutManager(binding.baseList.context)
        val adapter = PostsAdapter(source) { item -> presenter.onPostClick(item, source) }
        adapter.addAll(getList())
        binding.baseList.adapter = adapter
        binding.baseList.addItemDecoration(DevicesFragment.SpacingItemDecoration(binding.baseList.context.getDimenPx(R.dimen.dp8), true))
    }

    private fun getList(): List<Device.PostItem> = when (source) {
        SRC_DISCUSSIONS -> device.discussions
        SRC_FIRMWARES -> device.firmwares
        SRC_NEWS -> device.news
        else -> emptyList()
    }

    fun setSource(source: Int): SubDeviceFragment {
        this.source = source
        return this
    }

    companion object {
        const val SRC_DISCUSSIONS = 1
        const val SRC_FIRMWARES = 2
        const val SRC_NEWS = 3
    }
}
