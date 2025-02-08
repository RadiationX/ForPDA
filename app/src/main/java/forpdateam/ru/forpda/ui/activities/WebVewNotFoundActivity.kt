package forpdateam.ru.forpda.ui.activities

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.LocaleHelper
import forpdateam.ru.forpda.databinding.ActivityWvNotFoundBinding

/**
 * Created by radiationx on 23.07.17.
 */

class WebVewNotFoundActivity : AppCompatActivity(R.layout.activity_wv_not_found) {

    private val binding by viewBinding<ActivityWvNotFoundBinding>()

    private val nougatMsg = """Убедитесь, что сервис WebView установлен и активирован:
1. Включите режим разработчика на вашем Android-устройстве.

2.Зайдите в раздел «Для разработчиков» и нажмите по пункту «Сервис WebView».

3.Возможно, вы увидите там возможность выбрать между Chrome Stable и Android System WebView (или Google WebView, что одно и то же)."""

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.nougatplus.visibility =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) View.VISIBLE else View.GONE
        binding.nougatplus.text = nougatMsg


        binding.getInGp.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.webview")
            ).addFlags(FLAG_ACTIVITY_NEW_TASK)
            startActivity(
                Intent.createChooser(intent, "Открыть в").addFlags(FLAG_ACTIVITY_NEW_TASK)
            )
        }

        binding.getIn4pda.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://4pda.to/forum/index.php?showtopic=705513")
            ).addFlags(FLAG_ACTIVITY_NEW_TASK)
            startActivity(
                Intent.createChooser(intent, "Открыть в").addFlags(FLAG_ACTIVITY_NEW_TASK)
            )
        }

        binding.wvTryStart.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
                .putExtra(MainActivity.ARG_CHECK_WEBVIEW, false)
            startActivity(intent)
            finish()
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        App.get().onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
