package forpdateam.ru.forpda.ui.fragments.devdb.device

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import com.robohorse.pagerbullet.PagerBullet
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.presentation.devdb.device.DevicePresenter
import forpdateam.ru.forpda.presentation.devdb.device.DeviceView
import forpdateam.ru.forpda.ui.DimensionHelper
import forpdateam.ru.forpda.ui.activities.imageviewer.ImageViewerActivity
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.devdb.DevDbHelper
import forpdateam.ru.forpda.ui.fragments.devdb.device.comments.CommentsFragment
import forpdateam.ru.forpda.ui.fragments.devdb.device.posts.PostsFragment
import forpdateam.ru.forpda.ui.fragments.devdb.device.specs.SpecsFragment
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup
import java.util.*

/**
 * Created by radiationx on 08.08.17.
 */

class DeviceFragment : TabFragment(), DeviceView {
    private lateinit var imagesPager: PagerBullet
    private lateinit var tabLayout: TabLayout
    private lateinit var rating: TextView
    private lateinit var fragmentsPager: ViewPager
    private lateinit var progressBar: ProgressBar
    private var toolbarContent: RelativeLayout? = null

    private val dimensionsProvider = App.get().Di().dimensionsProvider

    private lateinit var copyLinkMenuItem: MenuItem
    private lateinit var shareMenuItem: MenuItem
    private lateinit var noteMenuItem: MenuItem
    private lateinit var toBrandMenuItem: MenuItem
    private lateinit var toBrandsMenuItem: MenuItem

    private var appBarOffset = 0

    @InjectPresenter
    lateinit var presenter: DevicePresenter

    @ProvidePresenter
    fun providePresenter(): DevicePresenter = DevicePresenter(
            App.get().Di().devDbRepository,
            App.get().Di().router,
            App.get().Di().linkHandler,
            App.get().Di().errorHandler
    )

    init {
        configuration.defaultTitle = App.get().getString(R.string.fragment_title_device)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        baseInflateFragment(inflater, R.layout.fragment_device)
        val viewStub = findViewById(R.id.toolbar_content) as ViewStub
        viewStub.layoutResource = R.layout.toolbar_device
        toolbarContent = viewStub.inflate() as RelativeLayout
        imagesPager = findViewById(R.id.images_pager) as PagerBullet
        progressBar = findViewById(R.id.progress_bar) as ProgressBar
        rating = findViewById(R.id.item_rating) as TextView
        fragmentsPager = findViewById(R.id.view_pager) as ViewPager

        tabLayout = TabLayout(context)
        val tabParams = CollapsingToolbarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM)
        tabParams.collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
        tabLayout.layoutParams = tabParams
        toolbarLayout.addView(tabLayout)

        val params = toolbarLayout.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
        toolbarLayout.layoutParams = params

        val newParams = toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
        newParams.collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
        newParams.bottomMargin = App.px48
        toolbar.layoutParams = newParams
        toolbar.requestLayout()
        return viewFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setCardsBackground()
        toolbarTitleView.setShadowLayer(App.px2.toFloat(), 0f, 0f, App.getColorFromAttr(context, R.attr.colorPrimary))
        toolbarSubtitleView.setShadowLayer(App.px2.toFloat(), 0f, 0f, App.getColorFromAttr(context, R.attr.colorPrimary))

        toolbarLayout.setExpandedTitleColor(Color.TRANSPARENT)
        toolbarLayout.setCollapsedTitleTextColor(Color.TRANSPARENT)
        toolbarLayout.isTitleEnabled = false

        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        tabLayout.setupWithViewPager(fragmentsPager)

        imagesPager.setIndicatorTintColorScheme(App.getColorFromAttr(context, R.attr.default_text_color), App.getColorFromAttr(context, R.attr.second_text_color))

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, offset ->
            appBarOffset = offset
            updateToolbarShadow()
        })

        if (configuration.isFitSystemWindow) {
            disposables.add(
                    dimensionsProvider
                            .observeDimensions()
                            .subscribe { dimensions ->
                                toolbarContent?.post {
                                    if (toolbarContent != null) {
                                        updateDimens(dimensions)
                                    }
                                }
                                updateDimens(dimensions)
                            }
            )
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
        val imagesAdapter = ImagesAdapter(context!!, urls, fullUrls)
        imagesPager.setAdapter(imagesAdapter)

        val pagerAdapter = FragmentPagerAdapter(childFragmentManager, data)
        fragmentsPager.adapter = pagerAdapter

        if (data.rating > 0) {
            rating.text = data.rating.toString()
            rating.background = App.getDrawableAttr(rating.context, R.attr.count_background)
            rating.background.colorFilter = DevDbHelper.getColorFilter(data.rating)
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
        NotesAddPopup.showAddNoteDialog(context, title, url)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    private inner class FragmentPagerAdapter(fm: FragmentManager, private val device: Device) : android.support.v4.app.FragmentPagerAdapter(fm) {
        private val fragments = ArrayList<Fragment>()
        private val titles = ArrayList<String>()

        init {
            if (!this.device.specs.isEmpty()) {
                fragments.add(SpecsFragment().setDevice(this.device))
                titles.add(App.get().getString(R.string.device_page_specs))
            }
            if (!this.device.comments.isEmpty()) {
                fragments.add(CommentsFragment().setDevice(this.device))
                val title = String.format(Locale.getDefault(),
                        App.get().getString(R.string.device_page_comments),
                        this.device.comments.size)
                titles.add(title)
            }
            if (!this.device.discussions.isEmpty()) {
                fragments.add(PostsFragment().setSource(PostsFragment.SRC_DISCUSSIONS).setDevice(this.device))
                val title = String.format(Locale.getDefault(),
                        App.get().getString(R.string.device_page_discussions),
                        this.device.discussions.size)
                titles.add(title)
            }
            if (!this.device.news.isEmpty()) {
                fragments.add(PostsFragment().setSource(PostsFragment.SRC_NEWS).setDevice(this.device))
                val title = String.format(Locale.getDefault(),
                        App.get().getString(R.string.device_page_news),
                        this.device.news.size)
                titles.add(title)
            }
            if (!this.device.firmwares.isEmpty()) {
                fragments.add(PostsFragment().setSource(PostsFragment.SRC_FIRMWARES).setDevice(this.device))
                val title = String.format(Locale.getDefault(),
                        App.get().getString(R.string.device_page_firmwares),
                        this.device.firmwares.size)
                titles.add(title)
            }
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
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
                ImageViewerActivity.startActivity(this@DeviceFragment.context!!, fullUrls, position)
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
            ImageLoader.getInstance().displayImage(urls[position], imageView, object : SimpleImageLoadingListener() {
                override fun onLoadingStarted(imageUri: String?, view: View?) {
                    progressBar.visibility = View.VISIBLE
                }

                override fun onLoadingCancelled(imageUri: String?, view: View?) {
                    progressBar.visibility = View.GONE
                }

                override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                    progressBar.visibility = View.GONE
                }

                override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
                    progressBar.visibility = View.GONE
                }
            })

        }
    }

    companion object {
        const val ARG_DEVICE_ID = "DEVICE_ID"
    }

}
