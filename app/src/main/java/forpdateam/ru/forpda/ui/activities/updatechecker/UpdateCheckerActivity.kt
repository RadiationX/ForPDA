package forpdateam.ru.forpda.ui.activities.updatechecker

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.BuildConfig
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.checker.UpdateData
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import kotlinx.android.synthetic.main.activity_updater.*
import forpdateam.ru.forpda.presentation.checker.CheckerPresenter
import forpdateam.ru.forpda.presentation.checker.CheckerView
import forpdateam.ru.forpda.ui.activities.MainActivity
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

/**
 * Created by radiationx on 24.07.17.
 */

@RuntimePermissions
class UpdateCheckerActivity : MvpAppCompatActivity(), CheckerView {

    companion object {
        const val ARG_FORCE = "force"
    }

    private val systemLinkHandler = App.get().Di().systemLinkHandler

    @InjectPresenter
    lateinit var presenter: CheckerPresenter

    @ProvidePresenter
    fun provideCheckerPresenter() = CheckerPresenter(
            App.get().Di().checkerRepository,
            App.get().Di().errorHandler
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_updater)
        MainActivity.setLightStatusBar(this, false)

        intent?.let {
            presenter.forceLoad = it.getBooleanExtra(ARG_FORCE, false)
        }

        toolbar.setNavigationOnClickListener { finish() }
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow_back)

        currentInfo.text = generateCurrentInfo(BuildConfig.VERSION_NAME, BuildConfig.BUILD_DATE)
    }

    override fun showUpdateData(update: UpdateData) {
        val currentVersionCode = BuildConfig.VERSION_CODE

        if (update.code > currentVersionCode) {
            updateInfo.text = generateCurrentInfo(update.name, update.date)
            addSection("Важно", update.important)
            addSection("Добавлено", update.added)
            addSection("Исправлено", update.fixed)
            addSection("Изменено", update.changed)

            updateInfo.visibility = View.VISIBLE
            updateButton.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE
        } else {
            updateInfo.text = "Нет обновлений, но вы можете загрузить текущую версию еще раз"
            updateInfo.visibility = View.VISIBLE
            updateContent.visibility = View.GONE
            divider.visibility = View.GONE
        }
        updateButton.visibility = View.VISIBLE
        updateButton.setOnClickListener {
            openDownloadDialog(update)
        }
    }

    private fun openDownloadDialog(update: UpdateData) {
        if (update.links.isEmpty()) {
            return
        }
        if (update.links.size == 1) {
            decideDownload(update.links.last())
            return
        }
        val titles = update.links.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
                .setTitle("Источник")
                .setItems(titles) { _, which ->
                    //Utils.externalLink(update.links[titles[which]].orEmpty())
                    decideDownload(update.links[which])
                }
                .show()
    }

    private fun decideDownload(link: UpdateData.UpdateLink) {
        when (link.type) {
            "file" -> systemDownloadWithPermissionCheck(link.url)
            "site" -> systemLinkHandler.handle(link.url)
            else -> systemLinkHandler.handle(link.url)
        }
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun systemDownload(url: String) {
        systemLinkHandler.handleDownload(url)
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun setRefreshing(isRefreshing: Boolean) {
        if (isRefreshing) {
            progressBar.visibility = View.VISIBLE
            updateInfo.visibility = View.GONE
            updateContent.visibility = View.GONE
            updateButton.visibility = View.GONE
            divider.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            updateInfo.visibility = View.VISIBLE
            updateContent.visibility = View.VISIBLE
            updateButton.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE
        }
    }

    private fun addSection(title: String, array: List<String>) {
        if (array.isEmpty()) {
            return
        }
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(0, 0, 0, (resources.displayMetrics.density * 24).toInt())

        val sectionTitle = TextView(this)
        sectionTitle.text = title
        sectionTitle.setPadding(0, 0, 0, (resources.displayMetrics.density * 8).toInt())
        sectionTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        //sectionTitle.setTextColor(ContextCompat.getColor(this, R.color.textDefault))
        root.addView(sectionTitle)

        val stringBuilder = StringBuilder()

        array.forEachIndexed { index, s ->
            stringBuilder.append("— ").append(s)
            if (index + 1 < array.size) {
                stringBuilder.append("<br>")
            }
        }

        val sectionText = TextView(this)
        sectionText.text = ApiUtils.spannedFromHtml(stringBuilder.toString())
        sectionText.setPadding((resources.displayMetrics.density * 8).toInt(), 0, 0, 0)
        //sectionText.setTextColor(ContextCompat.getColor(this, R.color.textDefault))
        root.addView(sectionText)

        updateContent.addView(root, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    private fun generateCurrentInfo(name: String?, date: String?): String {
        return String.format("Версия: %s\nСборка от: %s", name, date)
    }
}
