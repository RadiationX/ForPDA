package forpdateam.ru.forpda.presentation

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.MimeTypeUtil
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import io.appmetrica.analytics.AppMetrica
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SystemLinkHandler(
    private val context: Context,
    private val mainPreferencesHolder: MainPreferencesHolder,
    private val authHolder: AuthHolder
) : ISystemLinkHandler {
    override fun handle(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(
                Intent.createChooser(
                    intent,
                    context.getString(R.string.open_with)
                ).addFlags(FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: ActivityNotFoundException) {
            AppMetrica.reportError(e.message.orEmpty(), e)
        }
    }

    override fun handleDownload(url: String, inputFileName: String?) {
        val fileName = Utils.getFileNameFromUrl(url)
        val activity = App.getActivity()
        if (activity != null) {
            AlertDialog.Builder(activity)
                .setMessage(String.format(activity.getString(R.string.load_file), fileName))
                .setPositiveButton(activity.getString(R.string.ok)) { dialog, which ->
                    redirectDownload(
                        fileName,
                        url
                    )
                }
                .setNegativeButton(activity.getString(R.string.cancel), null)
                .show()
        } else {
            redirectDownload(fileName, url)
        }
    }

    private fun redirectDownload(fileName: String, url: String) {
        if (!authHolder.get().isAuth()) {
            App.getActivity()?.also { activity ->
                Utils.showNeedAuthDialog(activity)
            }
            return
        }
        Toast.makeText(
            context,
            String.format(context.getString(R.string.perform_request_link), fileName),
            Toast.LENGTH_SHORT
        ).show()

        GlobalScope.launch(Dispatchers.Main) {
            val response = coRunCatching {
                val request = NetworkRequest.Builder().url(url).withoutBody().build()
                App.get().Di().webClient.request(request)
            }.onFailure {
                it.printStackTrace()
                Toast.makeText(context, R.string.error_occurred, Toast.LENGTH_SHORT).show()
            }.getOrNull()
            if (response == null) {
                return@launch
            }

            coRunCatching {
                val activity = App.getActivity()
                val downloadUrl = response.redirect
                if (!mainPreferencesHolder.systemDownloader.get() || activity == null) {
                    externalDownloader(downloadUrl)
                } else {
                    val checkAction = {
                        try {
                            systemDownloader(fileName, downloadUrl)
                        } catch (exception: Exception) {
                            Toast.makeText(
                                context,
                                R.string.perform_loading_error,
                                Toast.LENGTH_SHORT
                            ).show()
                            externalDownloader(downloadUrl)
                        }
                    }
                    App.get().checkStoragePermission(checkAction, activity)
                }
            }.onFailure {
                AppMetrica.reportError(it.message.orEmpty(), it)
            }
        }
    }

    private fun systemDownloader(fileName: String, url: String) {
        val dm = App.getContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        request.setMimeType(MimeTypeUtil.getType(fileName))

        dm.enqueue(request)
    }

    private fun externalDownloader(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(
                Intent.createChooser(
                    intent,
                    context.getString(R.string.load_with)
                ).addFlags(FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: ActivityNotFoundException) {
            AppMetrica.reportError(e.message.orEmpty(), e)
        }

    }
}