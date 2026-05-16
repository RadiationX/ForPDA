package forpdateam.ru.forpda.ui.views.messagepanel.advanced

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.doOnLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import forpdateam.ru.forpda.App.Companion.get
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.ui.DimensionHelper.Dimensions
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by radiationx on 07.01.17.
 */
class AdvancedPopup(private val context: Context, private val messagePanel: MessagePanel) {
    private val popupWindow: PopupWindow
    private val fragmentContainer = messagePanel.fragmentContainer
    private var isShowingKeyboard = false
    private var stateListener: StateListener? = null

    private val dimensionsProvider = get().Di().dimensionsProvider

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        val popupView = View.inflate(context, R.layout.message_panel_advanced, null)
        val viewPager = popupView.findViewById<ViewPager>(R.id.pager)

        val viewList: MutableList<BasePanelItem> = ArrayList()
        viewList.add(CodesPanelItem(context, messagePanel))
        viewList.add(SmilesPanelItem(context, messagePanel))
        viewPager.adapter = MyPagerAdapter(viewList)

        (popupView.findViewById<View>(R.id.tab_layout) as TabLayout).setupWithViewPager(viewPager)

        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            dimensionsProvider.getDimensions().savedKeyboardHeight,
            false
        )

        popupWindow.setOnDismissListener {
            dimensionsProvider.getDimensions().isFakeKeyboardShow = false
            dimensionsProvider.update(dimensionsProvider.getDimensions())
        }

        popupView.findViewById<View>(R.id.delete_button).setOnClickListener { v: View? ->
            val messageField = messagePanel.getMessageField()
            var selectionStart = messageField!!.selectionStart
            var selectionEnd = messageField.selectionEnd
            if (selectionEnd < selectionStart && selectionEnd != -1) {
                val c = selectionStart
                selectionStart = selectionEnd
                selectionEnd = c
            }
            if (selectionStart != -1 && selectionStart != selectionEnd) {
                messageField.text.delete(selectionStart, selectionEnd)
                return@setOnClickListener
            }
            if (selectionStart > 0) {
                messageField.text.delete(selectionStart - 1, selectionStart)
            }
        }

        messagePanel.addAdvancedOnClickListener { v: View? ->
            if (popupWindow.isShowing) hidePopup()
            else showPopup()
        }
        dimensionsProvider
            .observeDimensions()
            .onEach { dimensions: Dimensions ->
                messagePanel.doOnLayout {
                    updateDimens(dimensions)
                }
                updateDimens(dimensions)
            }
            .launchIn(coroutineScope)
    }

    private fun updateDimens(dimensions: Dimensions) {
        if (dimensions.isKeyboardShow()) {
            popupWindow.height = dimensions.savedKeyboardHeight
            popupWindow.update()
            if (!isShowingKeyboard && popupWindow.isShowing) {
                hidePopup()
            }
            isShowingKeyboard = true
        } else if (isShowingKeyboard) {
            if (popupWindow.isShowing) {
                hidePopup()
            }
            isShowingKeyboard = false
        }
        messagePanel.setCanScrolling(!(isShowingKeyboard || popupWindow.isShowing))
    }

    private fun hidePopup() {
        val localDimensions = dimensionsProvider.getDimensions()
        messagePanel.advancedButton!!.setImageResource(R.drawable.ic_add)

        if (popupWindow.isShowing) {
            if (localDimensions.isFakeKeyboardShow) {
                localDimensions.isFakeKeyboardShow = false
                dimensionsProvider.update(localDimensions)
            }
            popupWindow.dismiss()
        }

        if (fragmentContainer.paddingBottom != 0) {
            Log.d("SUKA", "hidePopup SET PADDING 0")
            fragmentContainer.setPadding(
                fragmentContainer.paddingLeft,
                fragmentContainer.paddingTop,
                fragmentContainer.paddingRight,
                0
            )
        }

        if (stateListener != null) stateListener!!.onHide()

        messagePanel.setCanScrolling(true)
    }

    private fun showPopup() {
        val localDimensions = dimensionsProvider.getDimensions()
        messagePanel.advancedButton!!.setImageResource(R.drawable.ic_keyboard)

        if (!popupWindow.isShowing) {
            if (!localDimensions.isFakeKeyboardShow) {
                localDimensions.isFakeKeyboardShow = true
                dimensionsProvider.update(localDimensions)
            }
            popupWindow.showAtLocation(fragmentContainer, Gravity.BOTTOM, 0, 0)
        }

        Log.d(
            "FORPDA_LOG",
            "showPopup " + localDimensions.savedKeyboardHeight + " : " + fragmentContainer.paddingBottom + " : " + isShowingKeyboard
        )

        //fragmentContainer.setPadding(0, 0, 0, App.getKeyboardHeight());
        if (!isShowingKeyboard) {
            if (fragmentContainer.paddingBottom != localDimensions.savedKeyboardHeight) {
                Log.d("SUKA", "showPopup SET PADDING " + localDimensions.savedKeyboardHeight)
                fragmentContainer.setPadding(
                    fragmentContainer.paddingLeft,
                    fragmentContainer.paddingTop,
                    fragmentContainer.paddingRight,
                    localDimensions.savedKeyboardHeight
                )
            }
        } else {
            Log.d("SUKA", "showPopup SET PADDING " + 0)
            fragmentContainer.setPadding(
                fragmentContainer.paddingLeft,
                fragmentContainer.paddingTop,
                fragmentContainer.paddingRight,
                0
            )
        }

        if (stateListener != null) stateListener!!.onShow()

        messagePanel.setCanScrolling(false)
    }


    fun onBackPressed(): Boolean {
        if (!popupWindow.isShowing) return false
        hidePopup()
        return true
    }

    fun onResume() {
        //fragmentContainer.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }

    fun onPause() {
        //fragmentContainer.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        hidePopup()
    }

    fun onDestroy() {
        //fragmentContainer.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        coroutineScope.cancel()
        hidePopup()
    }

    fun hidePopupWindows() {
        hidePopup()
    }

    fun setStateListener(stateListener: StateListener?) {
        this.stateListener = stateListener
    }

    interface StateListener {
        fun onShow()

        fun onHide()
    }

    private inner class MyPagerAdapter(
        val pages: List<BasePanelItem>
    ) : PagerAdapter() {


        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val v: View = pages!![position]
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

        override fun getPageTitle(position: Int): CharSequence? {
            return pages[position].title
        }
    }
}
