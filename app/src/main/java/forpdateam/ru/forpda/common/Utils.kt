package forpdateam.ru.forpda.common

import android.content.*
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AlertDialog
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.presentation.Screen
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by isanechek on 30.07.16.
 */
object Utils {
    val isMM: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    fun getFileNameFromUrl(url: String): String {
        var fileName = url
        try {
            fileName = URLDecoder.decode(url, "CP1251")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        val cut = fileName.lastIndexOf('/')
        if (cut != -1) {
            fileName = fileName.substring(cut + 1)
        }
        return fileName
    }

    @JvmStatic
    fun copyToClipBoard(s: String?) {
        val clipboard = App.getContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", s)
        clipboard.setPrimaryClip(clip)
    }

    fun readFromClipboard(): String? {
        val clipboard = App.getContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val description = clipboard.primaryClipDescription
            val data = clipboard.primaryClip
            if (data != null && description != null && description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) return data.getItemAt(0).text.toString()
        }
        return null
    }

    fun shareText(text: String?) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, text)
        sendIntent.type = "text/plain"
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        App.get().startActivity(Intent.createChooser(sendIntent, App.get().getString(R.string.share)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun <T> checkNotNull(value: T?, message: String?): T {
        if (value == null) {
            throw NullPointerException(message)
        }
        return value
    }

    fun <T> checkNotNull(value: T?): T {
        if (value == null) {
            throw NullPointerException()
        }
        return value
    }

    fun longLog(msg: String) {
        val maxLogSize = 1000
        for (i in 0..msg.length / maxLogSize) {
            val start = i * maxLogSize
            var end = (i + 1) * maxLogSize
            end = if (end > msg.length) msg.length else end
            Log.v("LONG_LOG", msg.substring(start, end))
        }
    }

    fun log(msg: String) {
        Log.d("TEST", msg)
    }

    //copypast from forpda
    var dateFormat = SimpleDateFormat("dd.MM.yyyy")
    var parseDateTimeFormat = SimpleDateFormat("dd.MM.yyyy, HH:mm")
    val day: String
        get() {
            val nowCalendar = GregorianCalendar()
            return dateFormat.format(nowCalendar.time)
        }
    val yesterday: String
        get() {
            val nowCalendar = GregorianCalendar()
            nowCalendar.add(Calendar.DAY_OF_MONTH, -1)
            return dateFormat.format(nowCalendar.time)
        }

    fun getForumDateTime(date: Date?): String {
        return if (date == null) "" else parseDateTimeFormat.format(date)
    }

    fun getNewsDateTime(date: Date?): String {
        return if (date == null) "" else dateFormat.format(date)
    }

    fun parseForumDateTime(dateTime: String?): Date? {
        dateTime ?: return null
        try {
            val res = parseDateTimeFormat.parse(dateTime.replace("Сегодня", day).replace("Вчера", yesterday))
            val calendar: Calendar = GregorianCalendar()
            calendar.time = res
            val year = calendar[Calendar.YEAR]
            if (year < 100) calendar[Calendar.YEAR] = 2000 + year
            return calendar.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }

    fun showNeedAuthDialog(context: Context) {
        val router = App.get().Di().router
        AlertDialog.Builder(context)
                .setMessage("Необходимо войти в аккаунт 4pda")
                .setPositiveButton("Войти") { _, _ ->
                    router.navigateTo(Screen.Auth())
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
    }
}