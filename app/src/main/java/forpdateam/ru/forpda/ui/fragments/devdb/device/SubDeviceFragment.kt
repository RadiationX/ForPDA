package forpdateam.ru.forpda.ui.fragments.devdb.device

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.DeviceFragmentSpecsBinding
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.extensions.getDimenPx
import forpdateam.ru.forpda.extensions.getExtraNotNull
import forpdateam.ru.forpda.extensions.putExtra
import forpdateam.ru.forpda.extensions.quillMoxyPresenter
import forpdateam.ru.forpda.presentation.devdb.device.SubDeviceExtra
import forpdateam.ru.forpda.presentation.devdb.device.SubDevicePresenter
import forpdateam.ru.forpda.presentation.devdb.device.SubDeviceView
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment
import forpdateam.ru.forpda.ui.fragments.devdb.device.adapter.SubDeviceAdapter
import moxy.MvpAppCompatFragment

/**
 * Created by radiationx on 09.08.17.
 */

class SubDeviceFragment : MvpAppCompatFragment(R.layout.device_fragment_specs), SubDeviceView {

    companion object {
        private const val ARG_TYPE = "arg_type"

        fun newInstance(type: SubDeviceType): SubDeviceFragment {
            return SubDeviceFragment().putExtra {
                putSerializable(ARG_TYPE, type)
            }
        }
    }

    private val adapter = SubDeviceAdapter(
        articleClickListener = { presenter.onArticleClick(it) },
        topicClickListener = { presenter.onTopicClick(it) },
        commentClickListener = { presenter.onCommentClick(it) }
    )

    private val binding by viewBinding<DeviceFragmentSpecsBinding>()

    private val presenter by quillMoxyPresenter<SubDevicePresenter> {
        SubDeviceExtra(getExtraNotNull(ARG_TYPE))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.baseList.layoutManager = LinearLayoutManager(binding.baseList.context)
        binding.baseList.adapter = adapter
        binding.baseList.addItemDecoration(DevicesFragment.SpacingItemDecoration(binding.baseList.context.getDimenPx(R.dimen.dp8), true))
    }

    override fun bindSpecs(items: List<Device.Specs>) {
        adapter.bindSpecs(items)
    }

    override fun bindArticles(items: List<Device.Article>) {
        adapter.bindArticles(items)
    }

    override fun bindTopics(items: List<Device.Topic>) {
        adapter.bindTopics(items)
    }

    override fun bindComments(items: List<Device.Comment>) {
        adapter.bindComments(items)
    }
}
