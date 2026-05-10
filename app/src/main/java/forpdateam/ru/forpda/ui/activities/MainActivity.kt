package forpdateam.ru.forpda.ui.activities

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnLayout
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.daasuu.ei.Ease
import com.daasuu.ei.EasingInterpolator
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.DayNightHelper
import forpdateam.ru.forpda.common.LocaleHelper
import forpdateam.ru.forpda.databinding.ActivityMainBinding
import forpdateam.ru.forpda.presentation.main.MainPresenter
import forpdateam.ru.forpda.presentation.main.MainView
import forpdateam.ru.forpda.ui.DimensionHelper
import forpdateam.ru.forpda.ui.activities.updatechecker.SimpleUpdateChecker
import forpdateam.ru.forpda.ui.navigation.TabNavigator
import forpdateam.ru.forpda.ui.views.drawers.BottomDrawer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import kotlin.math.max

class MainActivity : MvpAppCompatActivity(R.layout.activity_main), MainView {
    val removeTabListener = { view: View -> backHandler(true) }


    private var checkWebView = true

    private val binding by viewBinding<ActivityMainBinding>()

    private lateinit var bottomDrawer: BottomDrawer
    private var firstStartAnimator: ObjectAnimator? = null

    val tabNavigator = TabNavigator(this, R.id.fragments_container)
    private val dimensionsProvider = App.get().Di().dimensionsProvider
    private val notificationPreferencesRepository = App.get().Di().notificationPreferencesHolder
    private val mainPreferencesRepository = App.get().Di().mainPreferencesHolder
    private val checkerRepository = App.get().Di().checkerRepository
    private val updateChecker by lazy { SimpleUpdateChecker(checkerRepository) }

    private var lang: String? = null

    @InjectPresenter
    lateinit var presenter: MainPresenter

    @ProvidePresenter
    fun providePresenter(): MainPresenter = MainPresenter(
        App.get().Di().router,
        App.get().Di().authHolder,
        App.get().Di().linkHandler,
        App.get().Di().menuRepository,
        App.get().Di().qmsInteractor,
        App.get().Di().otherPreferencesHolder,
        App.get().Di().mainPreferencesHolder,
        App.get().Di().errorHandler
    )

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
        App.get()
            .Di().dayNightHelper.setIsNight(DayNightHelper.isUiModeNight(resources.configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.DayNightAppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        if (intent != null) {
            checkWebView = intent.getBooleanExtra(ARG_CHECK_WEBVIEW, checkWebView)
        }
        if (checkWebView) {
            lifecycleScope.launch {
                val webviewFound = withContext(Dispatchers.Default) {
                    App.get().isWebViewFound(this@MainActivity)
                }
                if (webviewFound) {
                    return@launch
                }
                val intent = Intent(App.getContext(), WebVewNotFoundActivity::class.java)
                startActivity(intent)
                finish()
            }
        }


        Log.d(
            "kekeke",
            "oncreate UiMode: ${resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK}"
        )


        presenter.setIsRestored(savedInstanceState != null)
        intent?.data?.also {
            presenter.setStartLink(it.toString())
        }

        bottomDrawer = BottomDrawer(
            this,
            binding,
            tabNavigator,
            App.get().Di().router,
            App.get().Di().menuRepository,
            App.get().Di().mainPreferencesHolder
        )
        bottomDrawer.setListener(object : BottomDrawer.DrawerListener {
            override fun onHide() {
                hideKeyboard()
                cancelStartAnimation()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    window.navigationBarDividerColor = 0
                }
            }

            override fun onShow() {
                cancelStartAnimation()
            }

            override fun onSlide(slideOffset: Float) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && window.navigationBarDividerColor == 0) {
                    window.navigationBarDividerColor =
                        App.getColorFromAttr(this@MainActivity, R.attr.divider_line_bottom_nav)
                }
                val container = binding.fragmentsContainer
                val translate = -slideOffset * 0.1f * container.height
                container.translationY = translate
                cancelStartAnimation()
            }
        })

        val defaultStatusBarHeight =
            resources.getDimensionPixelSize(R.dimen.default_statusbar_height)
        val defaultKeyboardHeight = resources.getDimensionPixelSize(R.dimen.default_keyboard_height)

        binding.drawerLayout.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        DimensionHelper(
            binding.measureView,
            binding.measureRootContent,
            object : DimensionHelper.DimensionsListener {
                override fun onDimensionsChange(dimensions: DimensionHelper.Dimensions) {
                    Log.e("lalala", "Dim: $dimensions, bmr=${binding.bottomMenuRecycler.height}")
                    dimensionsProvider.update(dimensions)
                }
            },
            defaultStatusBarHeight,
            defaultKeyboardHeight
        )

        dimensionsProvider
            .observeDimensions()
            .onEach { dimensions ->
                binding.bottomMenuRecycler.doOnLayout {
                    binding.fragmentsContainer.also { updateDimens(dimensions) }
                }
            }
            .launchIn(lifecycleScope)

        if (notificationPreferencesRepository.updateEnabled.get()) {
            updateChecker.checkUpdate()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        tabNavigator.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.also { tabNavigator.onSaveInstanceState(it) }
    }

    override fun showFirstStartAnimation() {
        val height = resources.getDimensionPixelSize(R.dimen.dp48)
        firstStartAnimator =
            ObjectAnimator.ofFloat(binding.bottomSheet2, "translationY", 0f, -height.toFloat(), 0f)
                .apply {
                    interpolator = EasingInterpolator(Ease.BOUNCE_IN_OUT)
                    startDelay = 500
                    duration = 1500
                    repeatCount = 2
                    start()
                }
    }

    private fun cancelStartAnimation() {
        firstStartAnimator?.cancel()
        binding.bottomSheet2.translationY = 0f
    }

    private fun updateDimens(dimensions: DimensionHelper.Dimensions) {
        binding.fragmentsContainer.apply {
            val pb =
                dimensions.keyboardHeight + if (dimensions.isKeyboardShow() || dimensions.isFakeKeyboardShow) 0 else binding.bottomMenuRecycler.height
            Log.e(
                "lalala",
                "Post Dim: $dimensions, bmr=${binding.bottomMenuRecycler.height}, pb=$pb"
            )
            setPadding(
                paddingLeft,
                paddingTop,
                paddingRight,
                max(pb, 0)
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(LOG_TAG, "onNewIntent " + intent.toString())
        checkIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        bottomDrawer.onStart()
        App.get().Di().eventsController.start()
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        Log.d(LOG_TAG, "onResumeFragments")
        App.get().Di().navigatorHolder.setNavigator(tabNavigator)
    }


    override fun onResume() {
        super.onResume()
        Log.d(LOG_TAG, "onResume")
        if (lang == null) {
            lang = LocaleHelper.getLanguage(this)
        }
        // неработающий helper
        if (false && LocaleHelper.getLanguage(this) != lang) {
            val newContext = LocaleHelper.onAttach(this)
            AlertDialog.Builder(this)
                .setMessage(newContext.getString(R.string.lang_changed))
                .setPositiveButton(newContext.getString(R.string.ok)) { dialog, which ->
                    restartApplication(
                        this@MainActivity
                    )
                }
                .setNegativeButton(newContext.getString(R.string.cancel), null)
                .show()
        }
    }

    override fun onPause() {
        App.get().Di().navigatorHolder.removeNavigator()
        super.onPause()
        Log.d(LOG_TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        bottomDrawer.onStop()
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "onDestroy")
        super.onDestroy()
        bottomDrawer.destroy()
        updateChecker.cancel()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        App.get().onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun checkIntent(intent: Intent?) {
        if (intent == null || intent.data == null) {
            return
        }
        val url: String = intent.data?.toString().orEmpty()

        presenter.openLink(url)
        setIntent(null)
    }

    override fun onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed")
        if (bottomDrawer.isShown()) {
            bottomDrawer.hide()
            return
        }
        backHandler(false)
    }

    fun hideKeyboard() {
        currentFocus?.let {
            val iim = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            iim?.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    fun showKeyboard(view: View) {
        if (currentFocus != null) {
            val iim = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            iim?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun backHandler(fromToolbar: Boolean) {
        val active = tabNavigator.getCurrentFragment()
        if (active == null) {
            App.get().Di().router.exit()
            return
        }
        if (fromToolbar || !active.onBackPressed()) {
            hideKeyboard()
            tabNavigator.close(active.tag)
        }
    }


    companion object {
        val LOG_TAG = MainActivity::class.java.simpleName
        val DEF_TITLE = "ForPDA"
        val ARG_CHECK_WEBVIEW = "CHECK_WEBVIEW"

        fun restartApplication(activity: Activity) {
            val mStartActivity = Intent(activity, MainActivity::class.java)
            val mPendingIntentId = 123456
            val mPendingIntent = PendingIntent.getActivity(
                activity, mPendingIntentId, mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            val mgr = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
            activity.finish()
            System.exit(0)
        }

        fun setLightStatusBar(activity: Activity, value: Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val view = activity.window.decorView
                var flags = view.systemUiVisibility
                if (value) {
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
                view.systemUiVisibility = flags
            }
        }

        fun getDefaultLightStatusBar(context: Activity): Boolean {
            val typedValue = TypedValue()
            return if (context.theme.resolveAttribute(
                    R.attr.is_use_light_status_bar,
                    typedValue,
                    true
                )
            ) {
                if (typedValue.type == TypedValue.TYPE_INT_BOOLEAN) {
                    // Какого-то хрена boolean тут это -1=true, 0=false. Ну ОК гугл. ОК...
                    typedValue.data != 0
                } else {
                    throw Exception("Wtf bro?! I don't know this type: ${typedValue.type}")
                }
            } else {
                throw Exception("Wtf bro?! Where is_use_light_status_bar attr?!")
            }
        }
    }
}
