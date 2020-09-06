package forpdateam.ru.forpda.ui.fragments.devdb.device.specs

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment
import forpdateam.ru.forpda.ui.fragments.devdb.device.SubDeviceFragment

/**
 * Created by radiationx on 08.08.17.
 */

class SpecsFragment : SubDeviceFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.device_fragment_specs, container, false)
        val recyclerView = view.findViewById<View>(R.id.base_list) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        val adapter = SpecsAdapter()
        adapter.addAll(device.specs)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DevicesFragment.SpacingItemDecoration(App.px8, true))
        return view
    }
}
