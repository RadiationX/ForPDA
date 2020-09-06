package forpdateam.ru.forpda.ui.fragments.devdb.device.posts

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment
import forpdateam.ru.forpda.ui.fragments.devdb.device.SubDeviceFragment

/**
 * Created by radiationx on 09.08.17.
 */

class PostsFragment : SubDeviceFragment() {

    private var source = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.device_fragment_specs, container, false)
        //view.setBackgroundColor(App.getColorFromAttr(getContext(), R.attr.background_for_lists));
        val recyclerView = view.findViewById<View>(R.id.base_list) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        val adapter = PostsAdapter { item -> presenter.onPostClick(item, source) }
        adapter.setSource(source)

        adapter.addAll(getList())
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DevicesFragment.SpacingItemDecoration(App.px8, true))
        return view
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
