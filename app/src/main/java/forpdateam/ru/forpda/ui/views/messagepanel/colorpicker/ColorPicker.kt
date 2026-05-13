package forpdateam.ru.forpda.ui.views.messagepanel.colorpicker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel
import forpdateam.ru.forpda.ui.views.messagepanel.advanced.ColorsPanelItem
import forpdateam.ru.forpda.ui.views.messagepanel.advanced.PanelListItem

/**
 * Created by radiationx on 27.05.17.
 */
class ColorPicker(context: Context, messagePanel: MessagePanel, listener: (PanelListItem.Color) -> Unit) {

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layoutContainer = inflater.inflate(R.layout.color_picker_layout, null) as LinearLayout

        val viewPager = layoutContainer.findViewById<ViewPager>(R.id.color_picker_pager)
        val dialog = AlertDialog.Builder(context)
            .setView(layoutContainer)
            .create()
        val mainListener =  { item: PanelListItem.Color ->
            listener.invoke(item)
            dialog.dismiss()
        }
        val viewList: MutableList<ColorsPanelItem> = ArrayList()
        val materialColorsView = ColorsPanelItem(
            context = context,
            panel = messagePanel,
            colors = context.resources.getIntArray(R.array.md_colors).toList(),
            listener = mainListener,
            title = "Material"
        )
        val forumColorsView = ColorsPanelItem(
            context = context,
            panel = messagePanel,
            colors = context.resources.getIntArray(R.array.forum_colors).toList(),
            listener = mainListener,
            title = "Forum"
        )
        viewList.add(materialColorsView)
        viewList.add(forumColorsView)

        viewPager.adapter = MyPagerAdapter(viewList)
        (layoutContainer.findViewById<View>(R.id.color_picker_tab_layout) as TabLayout).setupWithViewPager(viewPager)

        dialog.show()
    }


    private class MyPagerAdapter(
        private val pages: List<ColorsPanelItem>
    ) : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val v: ViewGroup = pages[position]
            container.addView(v, 0)
            return v
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun getCount(): Int {
            return pages.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getPageTitle(position: Int): CharSequence {
            return pages[position].title
        }
    }
}
