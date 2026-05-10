package forpdateam.ru.forpda.ui.activities.updatechecker

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.AppBuildConfig
import forpdateam.ru.forpda.databinding.ActivityUpdaterBinding
import forpdateam.ru.forpda.entity.remote.checker.UpdateData
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.presentation.checker.CheckerPresenter
import forpdateam.ru.forpda.presentation.checker.CheckerView
import forpdateam.ru.forpda.ui.activities.MainActivity
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

/**
 * Created by radiationx on 24.07.17.
 */

@RuntimePermissions
class UpdateCheckerActivity : MvpAppCompatActivity(R.layout.activity_updater), CheckerView {

    companion object {
        const val ARG_FORCE = "force"
    }

    private val binding by viewBinding<ActivityUpdaterBinding>()

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

        MainActivity.setLightStatusBar(this, false)

        intent?.let {
            presenter.forceLoad = it.getBooleanExtra(ARG_FORCE, false)
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow_back)

        binding.currentInfo.text =
            generateCurrentInfo(AppBuildConfig.versionName, AppBuildConfig.buildDate)
    }

    override fun showUpdateData(update: UpdateData) {
        val currentVersionCode = AppBuildConfig.versionCode

        if (update.code > currentVersionCode) {
            binding.updateInfo.text = generateCurrentInfo(update.name, update.date)
            addSection("Важно", update.important)
            addSection("Добавлено", update.added)
            addSection("Исправлено", update.fixed)
            addSection("Изменено", update.changed)

            binding.updateInfo.visibility = View.VISIBLE
            binding.updateButton.visibility = View.VISIBLE
            binding.divider.visibility = View.VISIBLE
        } else {
            binding.updateInfo.text =
                "Нет обновлений, но вы можете загрузить текущую версию еще раз"
            binding.updateInfo.visibility = View.VISIBLE
            binding.updateContent.visibility = View.GONE
            binding.divider.visibility = View.GONE
        }
        binding.updateButton.visibility = View.VISIBLE
        binding.updateButton.setOnClickListener {
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
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun setRefreshing(isRefreshing: Boolean) {
        if (isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
            binding.updateInfo.visibility = View.GONE
            binding.updateContent.visibility = View.GONE
            binding.updateButton.visibility = View.GONE
            binding.divider.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.updateInfo.visibility = View.VISIBLE
            binding.updateContent.visibility = View.VISIBLE
            binding.updateButton.visibility = View.VISIBLE
            binding.divider.visibility = View.VISIBLE
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

        binding.updateContent.addView(
            root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun generateCurrentInfo(name: String?, date: String?): String {
        return String.format("Версия: %s\nСборка от: %s", name, date)
    }
}
