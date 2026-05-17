package forpdateam.ru.forpda.presentation

import android.Manifest
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
import androidx.core.content.getSystemService
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.MimeTypeUtil
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import io.appmetrica.analytics.AppMetrica
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.mintrocket.lib.mintpermissions.MintPermissionsController
import ru.mintrocket.lib.mintpermissions.ext.isGranted
import javax.inject.Inject

class SystemLinkHandler @Inject constructor(
    private val context: Context,
    private val mainPreferencesHolder: MainPreferencesHolder,
    private val webClient: IWebClient,
    private val permissionsController: MintPermissionsController,
    private val errorHandler: IErrorHandler,
    private val utils: Utils
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
        val fileName = utils.getFileNameFromUrl(url)
        val activity = App.getActivity()
        if (activity != null) {
            AlertDialog.Builder(activity)
                .setMessage(String.format(activity.getString(R.string.load_file), fileName))
                .setPositiveButton(activity.getString(R.string.ok)) { dialog, which ->
                    redirectDownload(fileName, url)
                }
                .setNegativeButton(activity.getString(R.string.cancel), null)
                .show()
        } else {
            redirectDownload(fileName, url)
        }
    }

    private fun redirectDownload(fileName: String, url: String) {
        Toast.makeText(
            context,
            String.format(context.getString(R.string.perform_request_link), fileName),
            Toast.LENGTH_SHORT
        ).show()

        GlobalScope.launch(Dispatchers.Main) {
            val response = coRunCatching {
                val request = NetworkRequest.Builder().url(url).withoutBody().build()
                webClient.request(request)
            }.onFailure {
                errorHandler.handle(it)
            }.getOrNull()

            if (response == null) {
                return@launch
            }

            coRunCatching {
                val downloadUrl = response.redirect
                if (!mainPreferencesHolder.systemDownloader.get()) {
                    externalDownloader(downloadUrl)
                } else {
                    systemDownloader(fileName, downloadUrl)
                }
            }.onFailure {
                AppMetrica.reportError(it.message.orEmpty(), it)
            }
        }
    }

    private suspend fun systemDownloader(fileName: String, url: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val permissionResult = permissionsController.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (!permissionResult.isGranted()) {
                externalDownloader(url)
                return
            }
        }

        try {
            val dm = context.getSystemService<DownloadManager>()!!
            val request = DownloadManager.Request(Uri.parse(url))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            request.setMimeType(MimeTypeUtil.getType(fileName))
            dm.enqueue(request)
        } catch (_: Exception) {
            Toast.makeText(context, R.string.perform_loading_error, Toast.LENGTH_SHORT).show()
            externalDownloader(url)
        }
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