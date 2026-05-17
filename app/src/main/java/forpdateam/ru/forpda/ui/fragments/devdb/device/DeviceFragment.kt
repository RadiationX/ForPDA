package forpdateam.ru.forpda.ui.fragments.devdb.device

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.tabs.TabLayout
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import com.robohorse.pagerbullet.PagerBullet
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.FragmentDeviceBinding
import forpdateam.ru.forpda.databinding.ToolbarDeviceBinding
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.extensions.getColorFromAttr
import forpdateam.ru.forpda.extensions.getDimenPx
import forpdateam.ru.forpda.extensions.quillMoxyPresenter
import forpdateam.ru.forpda.extensions.setBackgroundAttr
import forpdateam.ru.forpda.extensions.setBackgroundTintColor
import forpdateam.ru.forpda.presentation.devdb.device.DevicePresenter
import forpdateam.ru.forpda.presentation.devdb.device.DeviceView
import forpdateam.ru.forpda.ui.DimensionHelper
import forpdateam.ru.forpda.ui.DimensionsProvider
import forpdateam.ru.forpda.ui.activities.imageviewer.ImageViewerActivity
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.devdb.DevDbHelper
import forpdateam.ru.forpda.ui.fragments.devdb.device.comments.CommentsFragment
import forpdateam.ru.forpda.ui.fragments.devdb.device.posts.PostsFragment
import forpdateam.ru.forpda.ui.fragments.devdb.device.specs.SpecsFragment
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup
import forpdateam.ru.forpda.ui.fragments.tabBinding
import forpdateam.ru.forpda.ui.fragments.tabToolbarBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.quill.inject

/**
 * Created by radiationx on 08.08.17.
 */

class DeviceFragment : TabFragment(R.layout.fragment_device), DeviceView {

    private val binding by tabBinding(FragmentDeviceBinding::bind)
    private val toolbarBinding by tabToolbarBinding(ToolbarDeviceBinding::bind)

    private val imagesPager: PagerBullet
        get() = toolbarBinding.imagesPager
    private val rating: TextView
        get() = toolbarBinding.itemRating
    private val fragmentsPager: ViewPager
        get() = binding.viewPager
    private val progressBar: ProgressBar
        get() = binding.progressBar
    private val toolbarContent: RelativeLayout
        get() = toolbarBinding.root

    private val dimensionsProvider by inject<DimensionsProvider>()

    private lateinit var copyLinkMenuItem: MenuItem
    private lateinit var shareMenuItem: MenuItem
    private lateinit var noteMenuItem: MenuItem
    private lateinit var toBrandMenuItem: MenuItem
    private lateinit var toBrandsMenuItem: MenuItem

    private var appBarOffset = 0

    private val presenter by quillMoxyPresenter<DevicePresenter>()

    init {
        configuration.defaultTitle = getString(R.string.fragment_title_device)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            presenter.deviceId = getString(ARG_DEVICE_ID, null)
        }

        val transaction = childFragmentManager.beginTransaction()
        for (fragment in childFragmentManager.fragments) {
            transaction.remove(fragment)
        }
        transaction.commit()
        childFragmentManager.executePendingTransactions()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseInflateToolbar(R.layout.toolbar_device)

        val tabLayout = TabLayout(requireContext())
        val tabParams = CollapsingToolbarLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        )
        tabParams.collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
        tabLayout.layoutParams = tabParams
        toolbarLayout.addView(tabLayout)

        val params = toolbarLayout.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
        toolbarLayout.layoutParams = params

        val newParams = toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
        newParams.collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
        newParams.bottomMargin = toolbar.context.getDimenPx(R.dimen.dp48)
        toolbar.layoutParams = newParams
        toolbar.requestLayout()

        setCardsBackground()
        toolbarTitleView.setShadowLayer(
            toolbarTitleView.context.getDimenPx(R.dimen.dp2).toFloat(),
            0f,
            0f,
            toolbarTitleView.context.getColorFromAttr(androidx.appcompat.R.attr.colorPrimary)
        )
        toolbarSubtitleView.setShadowLayer(
            toolbarSubtitleView.context.getDimenPx(R.dimen.dp2).toFloat(),
            0f,
            0f,
            toolbarSubtitleView.context.getColorFromAttr(androidx.appcompat.R.attr.colorPrimary)
        )

        toolbarLayout.setExpandedTitleColor(Color.TRANSPARENT)
        toolbarLayout.setCollapsedTitleTextColor(Color.TRANSPARENT)
        toolbarLayout.isTitleEnabled = false

        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        tabLayout.setupWithViewPager(fragmentsPager)

        imagesPager.setIndicatorTintColorScheme(
            imagesPager.context.getColorFromAttr(R.attr.default_text_color),
            imagesPager.context.getColorFromAttr(R.attr.second_text_color)
        )

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, offset ->
            appBarOffset = offset
            updateToolbarShadow()
        })

        if (configuration.isFitSystemWindow) {
            dimensionsProvider
                .observeDimensions()
                .onEach { dimensions ->
                    toolbarContent.doOnLayout {
                        updateDimens(dimensions)
                    }
                    updateDimens(dimensions)
                }
                .launchIn(viewLifecycleOwner.lifecycleScope)
        }
    }

    override fun isShadowVisible(): Boolean {
        return appBarOffset != 0
    }

    private fun updateDimens(dimensions: DimensionHelper.Dimensions) {
        toolbarContent?.also {
            val params = it.layoutParams as CollapsingToolbarLayout.LayoutParams
            params.topMargin = dimensions.statusBar
            it.layoutParams = params
        }
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        copyLinkMenuItem = menu.add(R.string.copy_link)
            .setOnMenuItemClickListener {
                presenter.copyLink()
                true
            }

        shareMenuItem = menu.add(R.string.share)
            .setOnMenuItemClickListener {
                presenter.shareLink()
                true
            }

        noteMenuItem = menu.add(R.string.create_note)
            .setOnMenuItemClickListener {
                presenter.createNote()
                true
            }

        toBrandMenuItem = menu.add(R.string.devices)
            .setOnMenuItemClickListener {
                presenter.openDevices()
                true
            }

        toBrandsMenuItem = menu.add(R.string.devices)
            .setOnMenuItemClickListener {
                presenter.openBrands()
                true
            }

        refreshToolbarMenuItems(false)
    }

    override fun refreshToolbarMenuItems(enable: Boolean) {
        super.refreshToolbarMenuItems(enable)
        if (enable) {
            copyLinkMenuItem.isEnabled = true
            shareMenuItem.isEnabled = true
            noteMenuItem.isEnabled = true
            toBrandMenuItem.isVisible = true
            toBrandsMenuItem.isVisible = true
        } else {
            copyLinkMenuItem.isEnabled = false
            shareMenuItem.isEnabled = false
            noteMenuItem.isEnabled = false
            toBrandMenuItem.isVisible = false
            toBrandsMenuItem.isVisible = false
        }
    }

    override fun showData(data: Device) {
        progressBar.visibility = View.GONE
        toBrandMenuItem.title = "${data.catTitle} ${data.brandTitle}"
        toBrandsMenuItem.title = data.catTitle
        refreshToolbarMenuItems(true)
        setTitle(data.title)
        setTabTitle("${data.catTitle} ${data.brandTitle}: ${data.title}")
        setSubtitle("${data.catTitle} ${data.brandTitle}")


        val urls = ArrayList<String>()
        val fullUrls = ArrayList<String>()
        for (pair in data.images) {
            urls.add(pair.first)
            fullUrls.add(pair.second)
        }
        val imagesAdapter = ImagesAdapter(requireContext(), urls, fullUrls)
        imagesPager.setAdapter(imagesAdapter)

        val pagerAdapter = FragmentPagerAdapter(childFragmentManager, data)
        fragmentsPager.adapter = pagerAdapter

        if (data.rating > 0) {
            rating.text = data.rating.toString()
            rating.setBackgroundAttr(R.attr.count_background)
            rating.setBackgroundTintColor(DevDbHelper.getColor(data.rating))
            rating.visibility = View.VISIBLE
            if (!data.comments.isEmpty()) {
                rating.isClickable = true
                rating.setOnClickListener { fragmentsPager.setCurrentItem(1, true) }
            }

        } else {
            rating.visibility = View.GONE
        }
    }

    override fun showCreateNote(title: String, url: String) {
        NotesAddPopup.showAddNoteDialog(requireContext(), title, url)
    }

    private inner class FragmentPagerAdapter(
        fm: FragmentManager,
        private val device: Device
    ) : androidx.fragment.app.FragmentPagerAdapter(fm) {
        private val fragments = ArrayList<Fragment>()
        private val titles = ArrayList<String>()

        init {
            if (!this.device.specs.isEmpty()) {
                fragments.add(SpecsFragment().setDevice(this.device))
                titles.add(getString(R.string.device_page_specs))
            }
            if (!this.device.comments.isEmpty()) {
                fragments.add(CommentsFragment().setDevice(this.device))
                val title = getString(R.string.device_page_comments, this.device.comments.size)
                titles.add(title)
            }
            if (!this.device.discussions.isEmpty()) {
                fragments.add(
                    PostsFragment().setSource(PostsFragment.SRC_DISCUSSIONS).setDevice(this.device)
                )
                val title = getString(R.string.device_page_discussions, this.device.discussions.size)
                titles.add(title)
            }
            if (!this.device.news.isEmpty()) {
                fragments.add(
                    PostsFragment().setSource(PostsFragment.SRC_NEWS).setDevice(this.device)
                )
                val title = getString(R.string.device_page_news, this.device.news.size)
                titles.add(title)
            }
            if (!this.device.firmwares.isEmpty()) {
                fragments.add(
                    PostsFragment().setSource(PostsFragment.SRC_FIRMWARES).setDevice(this.device)
                )
                val title = getString(R.string.device_page_firmwares, this.device.firmwares.size)
                titles.add(title)
            }
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }
    }


    inner class ImagesAdapter(
        context: Context,
        private val urls: ArrayList<String>,
        private var fullUrls: ArrayList<String>
    ) : PagerAdapter() {
        //private SparseArray<View> views = new SparseArray<>();
        private val inflater: LayoutInflater = LayoutInflater.from(context)


        override fun getCount(): Int {
            return urls.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val imageLayout = inflater.inflate(R.layout.device_image_page, container, false)
            imageLayout.setOnClickListener {
                ImageViewerActivity.startActivity(
                    this@DeviceFragment.requireContext(),
                    fullUrls,
                    position
                )
            }
            container.addView(imageLayout, 0)
            loadImage(imageLayout, position)
            return imageLayout
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        private fun loadImage(imageLayout: View, position: Int) {
            val imageView = imageLayout.findViewById<View>(R.id.image_view) as ImageView
            val progressBar = imageLayout.findViewById<View>(R.id.progress_bar) as ProgressBar
            ImageLoader.getInstance()
                .displayImage(urls[position], imageView, object : SimpleImageLoadingListener() {
                    override fun onLoadingStarted(imageUri: String?, view: View?) {
                        progressBar.visibility = View.VISIBLE
                    }

                    override fun onLoadingCancelled(imageUri: String?, view: View?) {
                        progressBar.visibility = View.GONE
                    }

                    override fun onLoadingComplete(
                        imageUri: String?,
                        view: View?,
                        loadedImage: Bitmap?
                    ) {
                        progressBar.visibility = View.GONE
                    }

                    override fun onLoadingFailed(
                        imageUri: String?,
                        view: View?,
                        failReason: FailReason?
                    ) {
                        progressBar.visibility = View.GONE
                    }
                })

        }
    }

    companion object {
        const val ARG_DEVICE_ID = "DEVICE_ID"
    }

}
