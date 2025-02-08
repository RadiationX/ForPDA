package forpdateam.ru.forpda.ui.activities.imageviewer

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.viewpager.widget.ViewPager
import com.github.chrisbanes.photoview.OnPhotoTapListener
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.LocaleHelper
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.databinding.ActivityImgViewerBinding

/**
 * Created by radiationx on 24.05.17.
 */

class ImageViewerActivity : AppCompatActivity(R.layout.activity_img_viewer) {

    private val binding by viewBinding<ActivityImgViewerBinding>()

    private val currentImages = mutableListOf<String>()
    private val names = mutableListOf<String>()
    private var currentIndex = 0
    private val adapter: ImageViewerAdapter = ImageViewerAdapter()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.ImageViewTheme)
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding.imageViewerPullBack.setCallback(pullBackCallback)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.navigationIcon =
            ContextCompat.getDrawable(binding.toolbar.context, R.drawable.ic_arrow_back_white_24dp)
                ?.apply {
                    setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                }


        val extUrls = mutableListOf<String>()
        if (intent != null && intent.extras != null && intent.extras!!.containsKey(IMAGE_URLS_KEY)) {
            extUrls.addAll(intent.extras!!.getStringArrayList(IMAGE_URLS_KEY)!!)
        } else if (savedInstanceState != null && savedInstanceState.containsKey(IMAGE_URLS_KEY)) {
            extUrls.addAll(savedInstanceState.getStringArrayList(IMAGE_URLS_KEY)!!)
        }

        currentImages.addAll(extUrls)
        names.addAll(currentImages.map { Utils.getFileNameFromUrl(it) })

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_INDEX_KEY)) {
            currentIndex = savedInstanceState.getInt(SELECTED_INDEX_KEY, 0)
        } else if (intent != null && intent.extras != null && intent.extras!!.containsKey(
                SELECTED_INDEX_KEY
            )
        ) {
            currentIndex = intent.extras!!.getInt(SELECTED_INDEX_KEY, 0)
        }
        if (currentIndex < 0) {
            currentIndex = 0
        }


        binding.imgViewerPager.addOnPageChangeListener(object :
            ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                updateTitle(position)
            }
        })
        adapter.setTapListener(OnPhotoTapListener { view, x, y -> toggle() })
        adapter.bindItem(currentImages)
        binding.imgViewerPager.adapter = adapter
        binding.imgViewerPager.currentItem = currentIndex
        binding.imgViewerPager.clipChildren = false
        binding.toolbar.doOnLayout { updateTitle(currentIndex) }
    }

    private fun updateTitle(selectedPageIndex: Int) {
        currentIndex = selectedPageIndex
        binding.toolbar.title = names[selectedPageIndex]
        binding.toolbar.subtitle = String.format(
            getString(R.string.image_viewer_subtitle_Cur_All),
            selectedPageIndex + 1,
            currentImages.size
        )
    }

    private fun toggle() {
        if (supportActionBar?.isShowing == true) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        supportActionBar?.hide()
        setShowNavigationBar(false)
    }

    private fun show() {
        supportActionBar?.show()
        setShowNavigationBar(true)
    }

    private fun setShowNavigationBar(value: Boolean) {
        val view = window.decorView
        var flags = view.systemUiVisibility
        flags = if (value) {
            flags and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv()
        } else {
            flags or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
        view.systemUiVisibility = flags
    }

    private val pullBackCallback = object : PullBackLayout.Callback {
        override fun onPullStart() {}
        override fun onPull(@PullBackLayout.Direction direction: Int, progress: Float) {}
        override fun onPullCancel(@PullBackLayout.Direction direction: Int) {}
        override fun onPullComplete(@PullBackLayout.Direction direction: Int) {
            finish()
        }
    }

    companion object {
        const val IMAGE_URLS_KEY = "IMAGE_URLS_KEY"
        const val SELECTED_INDEX_KEY = "SELECTED_INDEX_KEY"

        @JvmStatic
        fun startActivity(context: Context, imageUrl: String) {
            val intent = Intent(context, ImageViewerActivity::class.java)
            val urls = ArrayList<String>()
            urls.add(imageUrl)
            intent.putExtra(IMAGE_URLS_KEY, urls)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        @JvmStatic
        fun startActivity(context: Context, imageUrls: ArrayList<String>, selectedIndex: Int) {
            val intent = Intent(context, ImageViewerActivity::class.java)
            intent.putExtra(IMAGE_URLS_KEY, imageUrls)
            intent.putExtra(SELECTED_INDEX_KEY, selectedIndex)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
