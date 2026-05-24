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
import androidx.core.view.doOnLayout
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.daasuu.ei.Ease
import com.daasuu.ei.EasingInterpolator
import com.github.terrakok.cicerone.NavigatorHolder
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.ActivityMainBinding
import forpdateam.ru.forpda.extensions.asImmutableFlag
import forpdateam.ru.forpda.extensions.getColorFromAttr
import forpdateam.ru.forpda.extensions.quillMoxyPresenter
import forpdateam.ru.forpda.model.interactors.events.EventsController
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.presentation.main.MainPresenter
import forpdateam.ru.forpda.presentation.main.MainView
import forpdateam.ru.forpda.ui.DimensionHelper
import forpdateam.ru.forpda.ui.DimensionsProvider
import forpdateam.ru.forpda.ui.activities.updatechecker.SimpleUpdateChecker
import forpdateam.ru.forpda.ui.navigation.TabNavigator
import forpdateam.ru.forpda.ui.views.drawers.BottomDrawer
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import moxy.MvpAppCompatActivity
import ru.radiationx.quill.get
import ru.radiationx.quill.inject
import kotlin.math.max

class MainActivity : MvpAppCompatActivity(R.layout.activity_main), MainView {
    val removeTabListener = { view: View -> backHandler(true) }

    private val binding by viewBinding<ActivityMainBinding>()

    private lateinit var bottomDrawer: BottomDrawer
    private var firstStartAnimator: ObjectAnimator? = null

    val tabNavigator = TabNavigator(this, R.id.fragments_container)

    private val router by inject<TabRouter>()
    private val navigatorHolder by inject<NavigatorHolder>()
    private val dimensionsProvider by inject<DimensionsProvider>()
    private val updateChecker by inject<SimpleUpdateChecker>()
    private val eventsController by inject<EventsController>()

    private val presenter by quillMoxyPresenter<MainPresenter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.DayNightAppTheme_NoActionBar)
        super.onCreate(savedInstanceState)

        Log.d(
            "kekeke",
            "oncreate UiMode: ${resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK}"
        )

        presenter.setIsRestored(savedInstanceState != null)
        intent?.data?.also {
            presenter.setStartLink(it.toString())
        }

        bottomDrawer = BottomDrawer(
            activity = this,
            binding = binding,
            tabNavigator = tabNavigator,
            router = get(),
            menuRepository = get(),
            mainPreferencesHolder = get()
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
                    window.navigationBarDividerColor = getColorFromAttr(R.attr.divider_line_bottom_nav)
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

        updateChecker.checkUpdate()
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
        eventsController.start()
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        Log.d(LOG_TAG, "onResumeFragments")
        navigatorHolder.setNavigator(tabNavigator)
    }


    override fun onResume() {
        super.onResume()
        Log.d(LOG_TAG, "onResume")
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
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
            router.exit()
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

        fun newIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }

        fun restartApplication(activity: Activity) {
            val mStartActivity = newIntent(activity)
            val mPendingIntentId = 123456
            val mPendingIntent = PendingIntent.getActivity(
                activity, mPendingIntentId, mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT.asImmutableFlag()
            )
            val mgr = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
            activity.finish()
            System.exit(0)
        }

        fun setLightStatusBar(activity: Activity, value: Boolean) {
            val view = activity.window.decorView
            var flags = view.systemUiVisibility
            if (value) {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            view.systemUiVisibility = flags
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
