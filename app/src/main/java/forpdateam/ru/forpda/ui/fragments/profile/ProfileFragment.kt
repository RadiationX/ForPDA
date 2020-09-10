package forpdateam.ru.forpda.ui.fragments.profile

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.*
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.github.rahatarmanahmed.cpv.CircularProgressView
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.BitmapUtils
import forpdateam.ru.forpda.common.LinkMovementMethod
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.presentation.profile.ProfilePresenter
import forpdateam.ru.forpda.presentation.profile.ProfileView
import forpdateam.ru.forpda.ui.activities.MainActivity
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.profile.adapters.ProfileAdapter
import forpdateam.ru.forpda.ui.views.ScrimHelper
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by radiationx on 03.08.16.
 */
class ProfileFragment : TabFragment(), ProfileAdapter.ClickListener, ProfileView {

    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var nick: TextView
    private lateinit var group: TextView
    private lateinit var sign: TextView
    private lateinit var avatar: ImageView
    private lateinit var progressView: CircularProgressView

    private var blurLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    private lateinit var copyLinkMenuItem: MenuItem
    private lateinit var writeMenuItem: MenuItem

    private lateinit var adapter: ProfileAdapter

    private val authHolder = App.get().Di().authHolder
    private val linkHandler = App.get().Di().linkHandler

    private var isResume = false
    private var isScrim = false

    private var lastBlurWidth = 0
    private var lastBlurHeight = 0

    @InjectPresenter
    lateinit var presenter: ProfilePresenter

    @ProvidePresenter
    fun providePresenter(): ProfilePresenter = ProfilePresenter(
            App.get().Di().profileRepository,
            App.get().Di().router,
            App.get().Di().linkHandler,
            App.get().Di().errorHandler,
            App.get().Di().schedulers
    )

    init {
        configuration.isFitSystemWindow = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var profileUrl: String? = null
        arguments?.apply {
            profileUrl = getString(TabFragment.ARG_TAB)
        }
        if (profileUrl.isNullOrEmpty()) {
            profileUrl = "https://4pda.ru/forum/index.php?showuser=${authHolder.get().userId}"
        }
        presenter.profileUrl = profileUrl
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        baseInflateFragment(inflater, R.layout.fragment_profile)
        val viewStub = findViewById(R.id.toolbar_content) as ViewStub
        viewStub.layoutResource = R.layout.toolbar_profile
        viewStub.inflate()
        nick = findViewById(R.id.profile_nick) as TextView
        group = findViewById(R.id.profile_group) as TextView
        sign = findViewById(R.id.profile_sign) as TextView
        avatar = findViewById(R.id.profile_avatar) as ImageView
        recyclerView = findViewById(R.id.profile_list) as androidx.recyclerview.widget.RecyclerView
        progressView = findViewById(R.id.profile_progress) as CircularProgressView

        val params = toolbarLayout.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED
        toolbarLayout.layoutParams = params
        return viewFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(recyclerView.context)
        adapter = ProfileAdapter()
        adapter.setClickListener(this)
        recyclerView.adapter = adapter

        toolbarLayout.setExpandedTitleColor(Color.TRANSPARENT)
        toolbarLayout.setCollapsedTitleTextColor(Color.TRANSPARENT)
        toolbarLayout.isTitleEnabled = true
        toolbarTitleView.visibility = View.GONE

        val scrimHelper = ScrimHelper(appBarLayout, toolbarLayout)
        scrimHelper.setScrimListener { scrim: Boolean ->
            isScrim = scrim
            if (scrim) {
                toolbar.navigationIcon?.clearColorFilter()
                toolbar.overflowIcon?.clearColorFilter()
            } else {
                toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                toolbar.overflowIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            }
            updateStatusBar()
        }

        toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        toolbar.overflowIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
    }

    override fun isShadowVisible(): Boolean {
        return false
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        copyLinkMenuItem = menu.add(R.string.copy_link)
                .setOnMenuItemClickListener {
                    presenter.copyUrl()
                    true
                }
        writeMenuItem = menu.add(R.string.write)
                .setIcon(App.getVecDrawable(context, R.drawable.ic_profile_toolbar_create))
                .setOnMenuItemClickListener {
                    presenter.navigateToQms()
                    true
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        refreshToolbarMenuItems(false)
    }

    override fun onResumeOrShow() {
        super.onResumeOrShow()
        isResume = true
        updateStatusBar()
    }

    override fun onPauseOrHide() {
        super.onPauseOrHide()
        isResume = false
        updateStatusBar()
    }

    private fun updateStatusBar() {
        val defaultSb = MainActivity.getDefaultLightStatusBar(activity!!)
        if (isResume) {
            MainActivity.setLightStatusBar(activity!!, isScrim && defaultSb)
        } else {
            MainActivity.setLightStatusBar(activity!!, defaultSb)
        }
    }

    override fun refreshToolbarMenuItems(enable: Boolean) {
        super.refreshToolbarMenuItems(enable)
        if (enable) {
            copyLinkMenuItem.isEnabled = true
        } else {
            copyLinkMenuItem.isEnabled = false
            writeMenuItem.isVisible = false
        }
    }

    override fun setRefreshing(isRefreshing: Boolean) {
        super.setRefreshing(isRefreshing)
        if (isRefreshing) {
            refreshToolbarMenuItems(false)
        }
    }

    override fun onSaveClick(text: String) {
        presenter.saveNote(text)
    }

    override fun onContactClick(item: ProfileModel.Contact) {
        presenter.onContactClick(item)
    }

    override fun onDeviceClick(item: ProfileModel.Device) {
        presenter.onDeviceClick(item)
    }

    override fun onStatClick(item: ProfileModel.Stat) {
        presenter.onStatClick(item)
    }

    override fun onSaveNote(success: Boolean) {
        Toast.makeText(context, getString(if (success) R.string.profile_note_saved else R.string.error_occurred), Toast.LENGTH_SHORT).show()
    }

    override fun showProfile(data: ProfileModel) {
        refreshToolbarMenuItems(true)
        adapter.setProfile(data)
        adapter.notifyDataSetChanged()

        setTabTitle(String.format(getString(R.string.profile_with_Nick), data.nick))
        setTitle(data.nick)
        nick.text = data.nick
        group.text = data.group
        if (data.sign != null) {
            sign.text = data.sign
            sign.visibility = View.VISIBLE
            sign.movementMethod = LinkMovementMethod { url -> linkHandler.handle(url, null) }
        }

        if (!data.contacts.isEmpty()) {
            val isMe = data.id == authHolder.get().userId
            writeMenuItem.isVisible = !isMe
        }
    }

    override fun showAvatar(bitmap: Bitmap) {
        if (blurLayoutListener == null) {
            blurLayoutListener = ViewTreeObserver.OnGlobalLayoutListener { blur(bitmap) }
            toolbarBackground.viewTreeObserver.addOnGlobalLayoutListener(blurLayoutListener)
        }

        avatar.startAnimation(AlphaAnimation(0f, 1f).apply {
            duration = 500
            fillAfter = true
        })
        avatar.setImageBitmap(bitmap)

        progressView.startAnimation(AlphaAnimation(1f, 0f).apply {

        })
        progressView.postDelayed({
            progressView.stopAnimation()
            progressView.visibility = View.GONE
        }, 500)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun blur(bkg: Bitmap) {
        val scaleFactor = 3f
        val radius = 4
        val blurWidth = toolbarBackground.width
        val blurHeight = toolbarBackground.height
        if (blurWidth <= 0 && blurHeight <= 0 || blurWidth == lastBlurWidth || blurHeight == lastBlurHeight) {
            return
        }
        lastBlurWidth = blurWidth
        lastBlurHeight = blurHeight
        val disposable = Single
                .fromCallable {
                    val overlay = BitmapUtils.centerCrop(bkg, lastBlurWidth, lastBlurHeight, scaleFactor)
                    BitmapUtils.fastBlur(overlay, radius, true)
                    overlay
                }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bitmap ->
                    toolbarBackground.startAnimation(AlphaAnimation(0f, 1f).apply {
                        duration = 500
                        fillAfter = true
                    })
                    toolbarBackground.setImageBitmap(bitmap)
                }, { throwable ->
                    throwable.printStackTrace()
                    Toast.makeText(App.getContext(), throwable.message, Toast.LENGTH_SHORT).show()
                })
        addToDisposable(disposable)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (blurLayoutListener != null) {
            toolbarBackground.viewTreeObserver.removeOnGlobalLayoutListener(blurLayoutListener)
            blurLayoutListener = null
        }
    }
}
