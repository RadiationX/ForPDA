package forpdateam.ru.forpda.ui.activities.imageviewer

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.viewpager.widget.ViewPager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.chrisbanes.photoview.OnPhotoTapListener
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.databinding.ActivityImgViewerBinding
import forpdateam.ru.forpda.extensions.mutateWithTint
import ru.radiationx.quill.inject

/**
 * Created by radiationx on 24.05.17.
 */

class ImageViewerActivity : AppCompatActivity(R.layout.activity_img_viewer) {

    private val binding by viewBinding<ActivityImgViewerBinding>()

    private val currentImages = mutableListOf<String>()
    private val names = mutableListOf<String>()
    private var currentIndex = 0
    private val adapter: ImageViewerAdapter = ImageViewerAdapter()
    private val utils by inject<Utils>()

    public override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.ImageViewTheme)
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding.imageViewerPullBack.setCallback(pullBackCallback)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.navigationIcon = binding.toolbar.context
            .getDrawable(R.drawable.ic_arrow_back_white_24dp)
            ?.mutateWithTint(Color.WHITE)


        val argUrls = intent?.getStringArrayListExtra(IMAGE_URLS_KEY).orEmpty()
        val argSelectedUrl = intent?.getStringExtra(SELECTED_URL_KEY)
        currentIndex = argUrls.indexOf(argSelectedUrl).coerceAtLeast(0)
        currentImages.addAll(argUrls)
        names.addAll(currentImages.map { utils.getFileNameFromUrl(it) })

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
        binding.toolbar.subtitle = getString(R.string.image_viewer_subtitle_Cur_All, selectedPageIndex + 1, currentImages.size)
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
        const val SELECTED_URL_KEY = "SELECTED_INDEX_KEY"

        fun createIntent(context: Context, urls: List<String>, selectedUrl: String?): Intent {
            val intent = Intent(context, ImageViewerActivity::class.java)
            intent.putExtra(IMAGE_URLS_KEY, ArrayList(urls))
            intent.putExtra(SELECTED_URL_KEY, selectedUrl)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            return intent
        }

        @JvmStatic
        fun startActivity(context: Context, imageUrl: String) {
            context.startActivity(createIntent(context, listOf(imageUrl), null))
        }

        @JvmStatic
        fun startActivity(context: Context, imageUrls: List<String>, selectedUrl: String?) {
            context.startActivity(createIntent(context, imageUrls, selectedUrl))
        }
    }
}
