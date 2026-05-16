package forpdateam.ru.forpda.ui.fragments.devdb.device.comments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.DeviceFragmentSpecsBinding
import forpdateam.ru.forpda.extensions.getDimenPx
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment
import forpdateam.ru.forpda.ui.fragments.devdb.device.SubDeviceFragment

/**
 * Created by radiationx on 09.08.17.
 */

class CommentsFragment : SubDeviceFragment(R.layout.device_fragment_specs) {

    private val binding by viewBinding<DeviceFragmentSpecsBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CommentsAdapter { item -> presenter.onCommentClick(item) }
        binding.baseList.layoutManager = LinearLayoutManager(binding.baseList.context)
        adapter.addAll(device.comments)
        binding.baseList.adapter = adapter
        binding.baseList.addItemDecoration(DevicesFragment.SpacingItemDecoration(binding.baseList.context.getDimenPx(R.dimen.dp8), true))
    }
}
