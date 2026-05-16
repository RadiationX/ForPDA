package forpdateam.ru.forpda.ui.fragments.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.extensions.getDimenPx

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
        view?.findViewById<RecyclerView>(androidx.preference.R.id.recycler_view)
            ?.also { list ->
                list.setPadding(0, 0, 0, 0)
                list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(
                        recyclerView: RecyclerView,
                        dx: Int,
                        dy: Int
                    ) {
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
            (activity as? AppCompatActivity)?.apply {
                supportActionBar?.apply {
                    elevation = if (isVisible) {
                        getDimenPx(R.dimen.dp2).toFloat()
                    } else {
                        0f
                    }
                }
            }
            lastIsVisible = isVisible
        }
    }
}
