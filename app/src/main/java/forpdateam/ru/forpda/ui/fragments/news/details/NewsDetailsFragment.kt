package forpdateam.ru.forpda.ui.fragments.news.details

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.FragmentArticleBinding
import forpdateam.ru.forpda.databinding.ToolbarNewsDetailsBinding
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.extensions.getDimenPx
import forpdateam.ru.forpda.extensions.mutateWithTint
import forpdateam.ru.forpda.extensions.putExtra
import forpdateam.ru.forpda.extensions.quillMoxyPresenter
import forpdateam.ru.forpda.model.interactors.news.ArticleInteractor
import forpdateam.ru.forpda.presentation.articles.detail.ArticleDetailPresenter
import forpdateam.ru.forpda.presentation.articles.detail.ArticleDetailView
import forpdateam.ru.forpda.ui.activities.MainActivity
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.TabTopScroller
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup
import forpdateam.ru.forpda.ui.fragments.tabBinding
import forpdateam.ru.forpda.ui.fragments.tabToolbarBinding
import forpdateam.ru.forpda.ui.views.ExtendedWebView
import forpdateam.ru.forpda.ui.views.ScrimHelper
import ru.radiationx.coretypes.ArticleId
import ru.radiationx.links.Link
import ru.radiationx.quill.inject
import ru.radiationx.quill.installModules
import ru.radiationx.quill.quillModule

/**
 * Created by isanechek on 8/19/17.
 */

class NewsDetailsFragment : TabFragment(R.layout.fragment_article), ArticleDetailView,
    TabTopScroller {

    companion object {
        private const val ARG_ID = "arg_id"
        private const val ARG_COMMENT_ID = "arg_comment_id"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_AUTHOR_NICK = "arg_author_nick"
        private const val ARG_DATE = "arg_date"
        private const val ARG_IMAGE_URL = "arg_image_url"
        private const val ARG_COMMENT_COUNT = "arg_comment_count"

        fun newInstanceLink(link: Link.Site.Details) = NewsDetailsFragment().putExtra {
            putParcelable(ARG_ID, link.articleId)
            putParcelable(ARG_COMMENT_ID, link.commentId)
        }

        fun newInstanceList(
            articleId: ArticleId,
            title: String,
            authorNick: String,
            date: String,
            imageUrl: String,
            commentCount: Int
        ) = NewsDetailsFragment().putExtra {
            putParcelable(ARG_ID, articleId)
            putString(ARG_TITLE, title)
            putString(ARG_AUTHOR_NICK, authorNick)
            putString(ARG_DATE, date)
            putString(ARG_IMAGE_URL, imageUrl)
            putInt(ARG_COMMENT_COUNT, commentCount)
        }
    }

    private val binding by tabBinding(FragmentArticleBinding::bind)
    private val toolbarBinding by tabToolbarBinding(ToolbarNewsDetailsBinding::bind)


    val fragmentsPager: ViewPager
        get() = binding.viewPager
    private val progressBar: ProgressBar
        get() = binding.progressBar
    private val imageProgressBar: ProgressBar
        get() = toolbarBinding.articleProgressBar
    private val detailsImage: ImageView
        get() = toolbarBinding.articleImage

    private val detailsTitle: TextView
        get() = toolbarBinding.articleTitle
    private val detailsNick: TextView
        get() = toolbarBinding.articleNick
    private val detailsCount: TextView
        get() = toolbarBinding.articleCommentsCount
    private val detailsDate: TextView
        get() = toolbarBinding.articleDate

    private var isResume = false
    private var isScrim = false

    private val interactor by inject<ArticleInteractor>()

    private val presenter by quillMoxyPresenter<ArticleDetailPresenter>()

    fun getAppBar() = appBarLayout

    init {
        configuration.defaultTitle = getString(R.string.fragment_title_news)
        configuration.isFitSystemWindow = true
    }

    public override fun attachWebView(webView: ExtendedWebView) {
        super.attachWebView(webView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installModules(quillModule {
            instance(ArticleInteractor.InitData())
            single<ArticleInteractor>()
        })
        Log.e("lalala", "onCreate " + this + " : " + arguments)
        arguments?.apply {
            interactor.initData.newsId = getInt(ARG_NEWS_ID, 0)
            interactor.initData.commentId = getInt(ARG_NEWS_COMMENT_ID, 0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseInflateToolbar(R.layout.toolbar_news_details)
        detailsImage.maxHeight = detailsImage.context.getDimenPx(R.dimen.dp24) * 10
        setScrollFlagsExitUntilCollapsed()

        val scrimHelper = ScrimHelper(appBarLayout, toolbarLayout)
        scrimHelper.setScrimListener { scrim1 ->
            isScrim = scrim1
            toolbarTitleView.isVisible = scrim1
            toolbar.apply {
                if (scrim1) {
                    navigationIcon = navigationIcon?.mutateWithTint(null)
                    overflowIcon = overflowIcon?.mutateWithTint(null)
                } else {
                    navigationIcon = navigationIcon?.mutateWithTint(Color.WHITE)
                    overflowIcon = overflowIcon?.mutateWithTint(Color.WHITE)
                }
            }
            updateStatusBar()
        }


        arguments?.apply {
            val newsTitle = getString(ARG_NEWS_TITLE)
            val newsNick = getString(ARG_NEWS_AUTHOR_NICK)
            val newsDate = getString(ARG_NEWS_DATE)
            val newsImageUrl = getString(ARG_NEWS_IMAGE)
            val newsCount = getInt(ARG_NEWS_COMMENTS_COUNT, -1)
            if (newsTitle != null) {
                setTitle(newsTitle)
                setTabTitle(getString(R.string.fragment_tab_title_article, newsTitle))
                detailsTitle.text = newsTitle
            }
            if (newsNick != null) {
                detailsNick.text = newsNick
            }
            if (newsCount != -1) {
                detailsCount.text = newsCount.toString()
            }
            if (newsDate != null) {
                detailsDate.text = newsDate
            }
            if (newsImageUrl != null) {
                showArticleImage(newsImageUrl)
            }
        }

        toolbarTitleView.isVisible = false
        toolbar.apply {
            navigationIcon = navigationIcon?.mutateWithTint(Color.WHITE)
            overflowIcon = overflowIcon?.mutateWithTint(Color.WHITE)
        }
        detailsNick.setOnClickListener { presenter.openAuthorProfile() }
    }

    override fun toggleScrollTop() {
        ((fragmentsPager.adapter as FragmentPagerAdapter).getItem(fragmentsPager.currentItem) as? TabTopScroller)?.toggleScrollTop()
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        menu.add(R.string.copy_link)
            .setOnMenuItemClickListener {
                presenter.copyLink()
                false
            }
        menu.add(R.string.share)
            .setOnMenuItemClickListener {
                presenter.shareLink()
                false
            }
        menu.add(R.string.create_note)
            .setOnMenuItemClickListener {
                presenter.createNote()
                false
            }
    }

    override fun onBackPressed(): Boolean {
        if (fragmentsPager.currentItem == 1) {
            fragmentsPager.currentItem = 0
            return true
        }
        return super.onBackPressed()
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

    override fun setRefreshing(isRefreshing: Boolean) {
        progressBar.visibility = if (isRefreshing) View.VISIBLE else View.GONE
    }

    override fun showArticle(data: DetailsPage) {
        setTitle(data.title)
        setTabTitle(getString(R.string.fragment_tab_title_article, data.title))
        detailsTitle.text = data.title
        detailsNick.text = data.author
        detailsDate.text = data.date
        detailsCount.text = data.commentsCount.toString()

        data.imgUrl?.also {
            showArticleImage(it)
        }

        val pagerAdapter = FragmentPagerAdapter(childFragmentManager)
        fragmentsPager.adapter = pagerAdapter
        if (interactor.initData.commentId > 0) {
            appBarLayout.setExpanded(false, true)
            fragmentsPager.setCurrentItem(1, true)
        }
    }

    override fun showCreateNote(title: String, url: String) {
        NotesAddPopup.showAddNoteDialog(requireContext(), title, url)
    }

    override fun showArticleImage(imageUrl: String) {
        ImageLoader.getInstance()
            .displayImage(imageUrl, detailsImage, object : SimpleImageLoadingListener() {
                override fun onLoadingStarted(imageUri: String?, view: View?) {
                    imageProgressBar.visibility = View.VISIBLE
                }

                override fun onLoadingComplete(
                    imageUri: String?,
                    view: View?,
                    loadedImage: Bitmap?
                ) {
                    imageProgressBar.visibility = View.GONE
                }
            })
    }

    private inner class FragmentPagerAdapter(
        fm: FragmentManager
    ) : androidx.fragment.app.FragmentPagerAdapter(fm) {
        private val fragments = ArrayList<Fragment>()
        private val titles = ArrayList<String>()

        init {
            fragments.add(ArticleContentFragment())
            titles.add(getString(R.string.news_page_content))

            fragments.add(ArticleCommentsFragment())
            titles.add(getString(R.string.news_page_comments))
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

}
