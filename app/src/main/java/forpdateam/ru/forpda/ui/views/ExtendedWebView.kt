package forpdateam.ru.forpda.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.ActionMode
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebSettings
import android.webkit.WebViewClient
import forpdateam.ru.forpda.App.Companion.getColorFromAttr
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.webview.DialogsHelper
import forpdateam.ru.forpda.common.webview.jsinterfaces.IBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue

/**
 * Created by radiationx on 01.11.16.
 */
class ExtendedWebView : NestedWebView, IBase {
    var direction: Int = DIRECTION_NONE
        private set
    private var relativeScale = 100
    private var fontScale = 1.0f
    private var paddingBottom = 0
    var isJsReady: Boolean = false

    private var onDirectionListener: OnDirectionListener? = null
    private var onScrollListener: OnScrollListener? = null
    private var audioManager: AudioManager? = null
    private val actionsForWebView: Queue<Runnable> = LinkedList<Runnable>()
    private var jsLifeCycleListener: JsLifeCycleListener? = null

    private var dialogsHelper: DialogsHelper? = null

    fun interface OnDirectionListener {
        fun onDirectionChanged(direction: Int)
    }

    fun interface OnScrollListener {
        fun onScrollChange(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int)
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    override fun onPause() {
        super.onPause()
        Log.e(LOG_TAG, "onPause " + this)
    }

    override fun onResume() {
        super.onResume()
        Log.e(LOG_TAG, "onResume " + this)
    }

    fun setOnDirectionListener(onDirectionListener: OnDirectionListener?) {
        this.onDirectionListener = onDirectionListener
    }

    fun setOnScrollListener(onScrollListener: OnScrollListener?) {
        this.onScrollListener = onScrollListener
    }

    override fun onScrollChanged(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY)
        if (onScrollListener != null) {
            onScrollListener!!.onScrollChange(scrollX, scrollY, oldScrollX, oldScrollY)
        }
        val newDirection: Int = if (scrollY > oldScrollY) DIRECTION_DOWN else DIRECTION_UP
        if (newDirection != direction) {
            direction = newDirection
            if (onDirectionListener != null) {
                onDirectionListener!!.onDirectionChanged(newDirection)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun init() {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        addJavascriptInterface(this, IBase.JS_BASE_INTERFACE)
        val settings = getSettings()
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        settings.builtInZoomControls = false
        settings.minimumFontSize = 1
        settings.minimumLogicalFontSize = 1
        settings.defaultFontSize = 16
        settings.textZoom = 100
        settings.javaScriptEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.allowFileAccessFromFileURLs = true
        settings.allowUniversalAccessFromFileURLs = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        setRelativeFontSize(16)
        setBackgroundColor(getColorFromAttr(context, R.attr.background_base))
        settings.textZoom = (resources.configuration.fontScale * 100).toInt()

        Log.e(
            "kokosina",
            "fontscale " + (resources.configuration.fontScale) + " : " + resources.configuration.densityDpi + " : " + resources.displayMetrics.density + " : " + resources.displayMetrics.densityDpi + " : " + resources.displayMetrics.scaledDensity + " : " + resources.displayMetrics.xdpi
        )
    }

    override fun loadData(data: String, mimeType: String?, encoding: String?) {
        isJsReady = false
        super.loadData(data, mimeType, encoding)
    }

    override fun loadDataWithBaseURL(
        baseUrl: String?,
        data: String,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?
    ) {
        isJsReady = false
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    override fun loadUrl(url: String) {
        isJsReady = false
        super.loadUrl(url)
    }

    override fun loadUrl(url: String, additionalHttpHeaders: MutableMap<String?, String?>) {
        isJsReady = false
        super.loadUrl(url, additionalHttpHeaders)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.e("kikosina", "onAttachedToWindow")
        //requestFocus();
        isJsReady = false
        /*for (Runnable action : actionsForWebView) {
            mHandler.removeCallbacks(action);
        }
        actionsForWebView.clear();*/
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.e("kikosina", "onDetachedFromWindow")
        isJsReady = false
        /*for (Runnable action : actionsForWebView) {
            mHandler.removeCallbacks(action);
        }
        actionsForWebView.clear();*/
    }

    //@Deprecated
    override fun setInitialScale(scaleInPercent: Int) {
        super.setInitialScale(scaleInPercent)
        Log.d(LOG_TAG, "SET INIT SCALE " + scaleInPercent)
        setPaddingBottom(paddingBottom)
    }


    //0.0f, 1.0f, 2.3f, etc
    fun setRelativeScale(scale: Float) {
        try {
            relativeScale = (scale * (resources.displayMetrics.density * 100)).toInt()
            fontScale = scale
        } catch (ignore: Exception) {
            ignore.printStackTrace()
        }
        setInitialScale(relativeScale)
    }

    fun setRelativeFontSize(fontSize: Int) {
        //setRelativeScale(fontSize / 16f);
        getSettings().defaultFontSize = fontSize
        //fontScale = fontSize / 16f;
        updatePaddingBottom()
    }

    fun updatePaddingBottom() {
        setPaddingBottom(paddingBottom)
    }

    fun setPaddingBottom(padding: Int) {
        Log.e(
            "kokosina",
            "setPaddingBottom " + padding + " : " + fontScale + " : " + ((paddingBottom / resources.displayMetrics.density) * (1 / fontScale))
        )
        paddingBottom = padding

        evalJs("setPaddingBottom(" + ((paddingBottom / resources.displayMetrics.density) * (1 / fontScale)) + ");")
    }

    fun evalJs(script: String) {
        //Log.d("EWV", "evalJs: " + script);
        try {
            evalJs(script, null)
        } catch (error: Exception) {
            Log.e("ExtendedWebView", "evalJs", error)
            loadUrl("javascript:" + script)
        }
    }

    fun evalJs(script: String, resultCallback: ValueCallback<String?>?) {
        syncWithJs(Runnable { evaluateJavascript(script, resultCallback) })
    }


    /*
     * JS LIFECYCLE
     * */
    @JavascriptInterface
    override fun playClickEffect() {
        runInUiThread(Runnable { this.tryPlayClickEffect() })
    }

    @JavascriptInterface
    override fun domContentLoaded() {
        runInUiThread(Runnable {
            Log.d(LOG_TAG, "domContentLoaded " + isJsReady)
            isJsReady = true
            for (action in actionsForWebView) {
                try {
                    runInUiThread(action)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
            actionsForWebView.clear()

            val actions = ArrayList<String>()
            if (jsLifeCycleListener != null) {
                try {
                    jsLifeCycleListener!!.onDomContentComplete(actions)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
            actions.add("nativeEvents.onNativeDomComplete();")

            var script: String? = ""
            for (action in actions) {
                script += action
            }
            evalJs(script!!)
        })
    }

    @JavascriptInterface
    override fun onPageLoaded() {
        runInUiThread(Runnable {
            Log.d(LOG_TAG, "onPageLoaded " + isJsReady)
            val actions = ArrayList<String>()
            if (jsLifeCycleListener != null) {
                try {
                    jsLifeCycleListener!!.onPageComplete(actions)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
            actions.add("nativeEvents.onNativePageComplete();")

            var script: String? = ""
            for (action in actions) {
                script += action
            }
            evalJs(script!!)
        })
    }


    fun tryPlayClickEffect() {
        try {
            audioManager!!.playSoundEffect(AudioManager.FX_KEY_CLICK)
        } catch (ignore: Exception) {
        }
    }

    fun runInUiThread(action: Runnable) {
        GlobalScope.launch(Dispatchers.Main.immediate) {
            action.run()
        }
    }

    fun setJsLifeCycleListener(jsLifeCycleListener: JsLifeCycleListener?) {
        this.jsLifeCycleListener = jsLifeCycleListener
    }

    interface JsLifeCycleListener {
        fun onDomContentComplete(actions: ArrayList<String>)

        fun onPageComplete(actions: ArrayList<String>)
    }


    fun syncWithJs(action: Runnable) {
        //Log.d(LOG_TAG, "syncWithJs " + isJsReady);
        if (!isJsReady) {
            actionsForWebView.add(action)
        } else {
            try {
                runInUiThread(action)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }


    /*
     * OVERRIDE CONTEXT MENU
     * */
    @JavascriptInterface
    fun onActionModeComplete() {
        runInUiThread(Runnable {
            if (currentActionMode != null) {
                currentActionMode!!.finish()
            }
        })
    }

    private var actionModeListener: OnStartActionModeListener? = null
    private var currentActionMode: ActionMode? = null

    interface OnStartActionModeListener {
        fun onCreate(actionMode: ActionMode, callback: ActionMode.Callback)

        fun onClick(actionMode: ActionMode, item: MenuItem): Boolean
    }

    fun setActionModeListener(actionModeListener: OnStartActionModeListener?) {
        this.actionModeListener = actionModeListener
    }

    fun setDialogsHelper(dialogsHelper: DialogsHelper?) {
        this.dialogsHelper = dialogsHelper
    }

    override fun startActionMode(callback: ActionMode.Callback): ActionMode? {
        return myActionMode(callback, 0)
    }

    override fun startActionMode(callback: ActionMode.Callback, type: Int): ActionMode? {
        return myActionMode(callback, type)
    }

    private fun myActionMode(callback: ActionMode.Callback, type: Int): ActionMode? {
        if (parent == null) {
            return null
        }

        val customCallback = getActionModeCallback(callback)
        val actionMode = super.startActionMode(customCallback, type)

        currentActionMode = actionMode
        if (actionModeListener != null) {
            actionModeListener!!.onCreate(actionMode, customCallback)
        }
        return actionMode
    }

    private fun getActionModeCallback(callback: ActionMode.Callback): ActionMode.Callback {
        return object : ActionMode.Callback2() {
            override fun onGetContentRect(mode: ActionMode?, view: View?, outRect: Rect?) {
                if (callback is ActionMode.Callback2) {
                    callback.onGetContentRect(mode, view, outRect)
                } else {
                    super.onGetContentRect(mode, view, outRect)
                }
            }

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return callback.onCreateActionMode(mode, menu)
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return callback.onPrepareActionMode(mode, menu)
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                if (actionModeListener != null && actionModeListener!!.onClick(mode, item)) {
                    return true
                }
                return callback.onActionItemClicked(mode, item)
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                currentActionMode = null
                callback.onDestroyActionMode(mode)
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?) {
        super.onCreateContextMenu(menu)
        requestFocusNodeHref(Handler(Handler.Callback { msg: Message? ->
            val result = getHitTestResult()
            if (dialogsHelper != null) {
                dialogsHelper!!.handleContextMenu(
                    context,
                    result.type,
                    result.extra!!,
                    msg!!.getData().get("url") as String?
                )
            }
            true
        }).obtainMessage())
    }

    fun endWork() {
        setActionModeListener(null)
        setWebChromeClient(null)
        setWebViewClient(WebViewClient())
        loadUrl("about:blank")
        clearHistory()
        clearSslPreferences()
        clearDisappearingChildren()
        clearFocus()
        clearFormData()
        clearMatches()
    }

    companion object {
        private val LOG_TAG: String = ExtendedWebView::class.java.getSimpleName()
        const val DIRECTION_NONE: Int = 0
        const val DIRECTION_UP: Int = 1
        const val DIRECTION_DOWN: Int = 2
    }
}
