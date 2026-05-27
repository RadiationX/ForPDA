package forpdateam.ru.forpda.ui.fragments.profile

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.rahatarmanahmed.cpv.CircularProgressView
import com.google.android.material.appbar.AppBarLayout
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.BitmapUtils
import forpdateam.ru.forpda.common.LinkMovementMethod
import forpdateam.ru.forpda.databinding.FragmentProfileBinding
import forpdateam.ru.forpda.databinding.ToolbarProfileBinding
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.extensions.getExtraNotNull
import forpdateam.ru.forpda.extensions.mutateWithTint
import forpdateam.ru.forpda.extensions.putExtra
import forpdateam.ru.forpda.extensions.quillMoxyPresenter
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.profile.ProfileExtra
import forpdateam.ru.forpda.presentation.profile.ProfilePresenter
import forpdateam.ru.forpda.presentation.profile.ProfileView
import forpdateam.ru.forpda.ui.activities.MainActivity
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.profile.adapters.ProfileAdapter
import forpdateam.ru.forpda.ui.fragments.tabBinding
import forpdateam.ru.forpda.ui.fragments.tabToolbarBinding
import forpdateam.ru.forpda.ui.views.ScrimHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.radiationx.coretypes.UserId
import ru.radiationx.quill.inject

/**
 * Created by radiationx on 03.08.16.
 */
class ProfileFragment : TabFragment(R.layout.fragment_profile), ProfileAdapter.ClickListener,
    ProfileView {

    companion object {
        private const val ARG_ID = "arg_id"
        fun newInstance(userId: UserId) = ProfileFragment().putExtra {
            putParcelable(ARG_ID, userId)
        }
    }

    private val binding by tabBinding(FragmentProfileBinding::bind)
    private val toolbarBinding by tabToolbarBinding(ToolbarProfileBinding::bind)

    private val recyclerView: RecyclerView
        get() = binding.profileList
    private val nick: TextView
        get() = toolbarBinding.profileNick
    private val group: TextView
        get() = toolbarBinding.profileGroup
    private val sign: TextView
        get() = toolbarBinding.profileSign
    private val avatar: ImageView
        get() = toolbarBinding.profileAvatar
    private val progressView: CircularProgressView
        get() = toolbarBinding.profileProgress

    private var blurLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    private lateinit var copyLinkMenuItem: MenuItem
    private lateinit var writeMenuItem: MenuItem

    private lateinit var adapter: ProfileAdapter

    private val authHolder by inject<AuthHolder>()
    private val linkHandler by inject<LinkHandler>()

    private var isResume = false
    private var isScrim = false

    private var lastBlurWidth = 0
    private var lastBlurHeight = 0

    private val presenter by quillMoxyPresenter<ProfilePresenter>{
        ProfileExtra(getExtraNotNull(ARG_ID))
    }

    init {
        configuration.isFitSystemWindow = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        baseInflateToolbar(R.layout.toolbar_profile)

        val params = toolbarLayout.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED
        toolbarLayout.layoutParams = params
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
            LinearLayoutManager(recyclerView.context)
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
            toolbar.apply {
                if (scrim) {
                    navigationIcon = navigationIcon?.mutateWithTint(null)
                    overflowIcon = overflowIcon?.mutateWithTint(null)
                } else {
                    navigationIcon = navigationIcon?.mutateWithTint(Color.WHITE)
                    overflowIcon = overflowIcon?.mutateWithTint(Color.WHITE)
                }
            }
            updateStatusBar()
        }

        toolbar.apply {
            navigationIcon = navigationIcon?.mutateWithTint(Color.WHITE)
            overflowIcon = overflowIcon?.mutateWithTint(Color.WHITE)
        }
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
            .setIcon(R.drawable.ic_profile_toolbar_create)
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
        val defaultSb = MainActivity.getDefaultLightStatusBar(requireActivity())
        if (isResume) {
            MainActivity.setLightStatusBar(requireActivity(), isScrim && defaultSb)
        } else {
            MainActivity.setLightStatusBar(requireActivity(), defaultSb)
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

    override fun onLinkClick(url: String?) {
        url?.let { linkHandler.handle(it) }
    }

    override fun onSaveNote(success: Boolean) {
        Toast.makeText(
            requireContext(),
            getString(if (success) R.string.profile_note_saved else R.string.error_occurred),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun showProfile(data: ProfileModel) {
        refreshToolbarMenuItems(true)
        adapter.setProfile(data)
        adapter.notifyDataSetChanged()

        setTabTitle(getString(R.string.profile_with_Nick, data.user.nick))
        setTitle(data.user.nick)
        nick.text = data.user.nick
        group.text = data.group
        if (data.sign != null) {
            sign.text = data.sign
            sign.visibility = View.VISIBLE
            sign.movementMethod = LinkMovementMethod { url -> linkHandler.handle(url) }
        }

        if (data.contacts.isNotEmpty()) {
            val isMe = data.user.id == authHolder.get().asAuth()?.userId
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
        viewLifecycleOwner.lifecycleScope.launch {
            coRunCatching {
                withContext(Dispatchers.Default) {
                    val overlay =
                        BitmapUtils.centerCrop(bkg, lastBlurWidth, lastBlurHeight, scaleFactor)
                    BitmapUtils.fastBlur(overlay, radius, true)
                }
            }.onSuccess {
                toolbarBackground.startAnimation(AlphaAnimation(0f, 1f).apply {
                    duration = 500
                    fillAfter = true
                })
                toolbarBackground.setImageBitmap(it)
            }.onFailure {
                it.printStackTrace()
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (blurLayoutListener != null) {
            toolbarBackground.viewTreeObserver.removeOnGlobalLayoutListener(blurLayoutListener)
            blurLayoutListener = null
        }
    }
}
