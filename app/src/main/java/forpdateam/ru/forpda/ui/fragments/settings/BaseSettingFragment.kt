package forpdateam.ru.forpda.ui.fragments.settings

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.widget.RecyclerView
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.ui.activities.SettingsActivity

/**
 * Created by radiationx on 24.09.17.
 */

open class BaseSettingFragment : PreferenceFragmentCompat() {

    private var listScrollY = 0
    private var lastIsVisible = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.findViewById<RecyclerView>(android.support.v7.preference.R.id.recycler_view)?.also { list ->
            list.setPadding(0, 0, 0, 0)
            list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    listScrollY = recyclerView.computeVerticalScrollOffset()
                    updateToolbarShadow()
                }
            })
        }
        updateToolbarShadow()
        setDividerHeight(0)
    }

    private fun updateToolbarShadow() {
        val isVisible = listScrollY > 0
        if (lastIsVisible != isVisible) {
            (activity as? SettingsActivity)?.supportActionBar?.elevation = if (isVisible) App.px2.toFloat() else 0f
            lastIsVisible = isVisible
        }
    }
}
