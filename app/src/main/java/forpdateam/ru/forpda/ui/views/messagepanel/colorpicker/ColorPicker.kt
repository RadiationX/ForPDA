package forpdateam.ru.forpda.ui.views.messagepanel.colorpicker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.thebluealliance.spectrum.SpectrumPalette
import forpdateam.ru.forpda.R

/**
 * Created by radiationx on 27.05.17.
 */
class ColorPicker(context: Context, listener: SpectrumPalette.OnColorSelectedListener?) {
    private val titles = arrayOf("Material", "Forum")

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layoutContainer = inflater.inflate(R.layout.color_picker_layout, null) as LinearLayout


        val viewPager = layoutContainer.findViewById<ViewPager>(R.id.color_picker_pager)

        val viewList: MutableList<ScrollView> = ArrayList()
        val scrollView = ScrollView(context)
        val scrollView1 = ScrollView(context)
        val materialColors = SpectrumPalette(context)
        materialColors.setColors(context.resources.getIntArray(R.array.md_colors))
        val forumColors = SpectrumPalette(context)
        forumColors.setColors(context.resources.getIntArray(R.array.forum_colors))
        scrollView.addView(materialColors)
        scrollView1.addView(forumColors)
        viewList.add(scrollView)
        viewList.add(scrollView1)

        viewPager.adapter =
            MyPagerAdapter(
                viewList
            )
        (layoutContainer.findViewById<View>(R.id.color_picker_tab_layout) as TabLayout).setupWithViewPager(
            viewPager
        )
        val dialog = AlertDialog.Builder(context)
            .setView(layoutContainer)
            .show()
        val mainListener = SpectrumPalette.OnColorSelectedListener { i: Int ->
            listener?.onColorSelected(i)
            dialog.dismiss()
        }
        materialColors.setOnColorSelectedListener(mainListener)
        forumColors.setOnColorSelectedListener(mainListener)
    }


    private inner class MyPagerAdapter(
        private val pages: List<ScrollView>
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
            return titles[position]
        }
    }
}
