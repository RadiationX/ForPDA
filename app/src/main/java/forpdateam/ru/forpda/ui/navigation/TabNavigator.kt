package forpdateam.ru.forpda.ui.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.github.terrakok.cicerone.Back
import com.github.terrakok.cicerone.BackTo
import com.github.terrakok.cicerone.Command
import com.github.terrakok.cicerone.Forward
import com.github.terrakok.cicerone.Navigator
import com.github.terrakok.cicerone.Replace
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.ui.activities.MainActivity
import forpdateam.ru.forpda.ui.activities.SettingsActivity
import forpdateam.ru.forpda.ui.activities.imageviewer.ImageViewerActivity
import forpdateam.ru.forpda.ui.activities.updatechecker.UpdateCheckerActivity
import forpdateam.ru.forpda.ui.fragments.TabFragment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject

class TabNavigator(
    private val activity: FragmentActivity,
    private val containerId: Int
) : Navigator {

    companion object {
        private const val TAG_PREFIX = "Tab_"
    }

    private val fragmentManager by lazy { activity.supportFragmentManager }
    val tabController by lazy { TabController() }

    private val subscribers = mutableListOf<TabFragment>()
    private val subscribersState = MutableStateFlow<List<TabFragment>>(subscribers)

    init {

    }

    fun onRestoreInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.getString("tab_controller_json")?.also {
            Log.e("TabController", "restore tab_controller_json: $it")
            tabController.onRestoreInstanceState(JSONObject(it))
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        tabController.onSaveInstanceState().toString().also {
            Log.e("TabController", "save tab_controller_json: $it")
            outState.putString("tab_controller_json", it)
        }
    }

    fun subscribe(tab: TabFragment) {
        Log.e("TabNavigator", "subscribe $tab")
        subscribers.add(tab)
        subscribersState.value = subscribers
    }

    fun unsubscribe(tab: TabFragment) {
        Log.e("TabNavigator", "unsubscribe $tab")
        subscribers.remove(tab)
        subscribersState.value = subscribers
    }

    fun notifyUpdate(tab: TabFragment) {
        Log.e("TabNavigator", "notifyUpdate $tab")
        subscribersState.value = subscribers
    }

    fun observeSubscribers(): Flow<List<TabFragment>> = subscribersState

    fun getCurrentFragment(): TabFragment? {
        return tabController.getCurrent()?.let {
            getByTag(it.tag)
        }
    }

    fun select(tabTag: String?) {
        if (tabTag == null) {
            Log.e("TabNavigator", "select CANCEL: tabTag==null")
            return
        }
        val fragment = getByTag(tabTag)
        Log.e("TabNavigator", "select tag=$tabTag fr=$fragment")
        tabController.setCurrent(tabTag)
        updateFragmentsState()
    }

    fun close(tabTag: String?) {
        if (tabTag == null) {
            Log.e("TabNavigator", "close CANCEL: tabTag==null")
            return
        }
        val fragment = getByTag(tabTag)
        Log.e("TabNavigator", "close tag=$tabTag fr=$fragment")
        if (tabController.getList().size <= 1) {
            exit()
        } else {
            fragmentManager
                .beginTransaction()
                .remove(fragment!!)
                .commit()
            tabController.remove(tabTag)
            updateFragmentsState()
        }
    }

    fun closeOthers() {
        val transaction = fragmentManager.beginTransaction()
        val itemTags =
            tabController.getList().map { it.tag }.filter { it != tabController.getCurrent()?.tag }
        Log.e("TabNavigator", "closeOthers")
        itemTags.forEach { itemTag ->
            getByTag(itemTag)?.also { fragment ->
                Log.e("TabNavigator", "closeOthers item=${itemTag} fr=$fragment")
                transaction.remove(fragment)
                tabController.remove(itemTag)
            }
        }
        transaction.commit()
        updateFragmentsState()
    }

    private fun updateFragmentsState() {
        /*tabController.getCurrent()?.tag?.let { getByTag(it) }?.also { fragment ->
            fragmentManager
                    .beginTransaction()
                    .show(fragment)
                    .commit()
        }*/
        tabController.printTabItems("TabNavigator")
        Log.e("TabNavigator", "updateFragmentsState")
        val transaction = fragmentManager.beginTransaction()

        val itemFragments = tabController.getList().map { Pair(it, getByTag(it.tag)) }

        itemFragments.forEach {
            if (it.first.tag != tabController.getCurrent()?.tag) {
                it.second?.also { fragment -> transaction.hide(fragment) }
            }
        }
        itemFragments.forEach {
            if (it.first.tag == tabController.getCurrent()?.tag) {
                it.second?.also { fragment -> transaction.show(fragment) }
            }
        }

        transaction.commit()
        subscribersState.value = subscribers
    }

    private fun getByTag(tag: String): TabFragment? {
        val result = fragmentManager.findFragmentByTag(tag) as TabFragment?
        Log.e(
            "TabNavigator",
            "getByTag tag=$tag, tab=${tabController.getCurrent()?.tag}, fr=$result"
        )
        return result
    }

    private fun genTag() = TAG_PREFIX + System.currentTimeMillis()

    override fun applyCommands(commands: Array<out Command>) {
        fragmentManager.executePendingTransactions()
        commands.forEach {
            applyCommand(it)
        }
    }

    private fun applyCommand(command: Command) {
        when (command) {
            is Forward -> forward(command)
            is Back -> back()
            is Replace -> replace(command)
            is BackTo -> backTo(command)
            is SystemMessage -> showSystemMessage(command)
        }
    }

    private fun forward(command: Forward) {
        val newScreen = command.screen as Screen
        createActivityIntent(activity, newScreen)?.also {
            checkAndStartActivity(it)
            return
        }

        tabController.findAlone(newScreen)?.also {
            tabController.setCurrent(it.tag)
            updateFragmentsState()
            return
        }

        val newFragment = createFragment(newScreen)
        val tag = genTag()

        Log.e("TabNavigator", "forward f=$newFragment")
        fragmentManager
            .beginTransaction()
            .add(containerId, newFragment, tag)
            .commit()
        tabController.addNew(tag, newScreen)
        updateFragmentsState()
    }

    private fun back() {
        if (tabController.getList().size <= 1) {
            exit()
        } else {
            tabController.getCurrent()?.also { tab ->
                val fragment = getByTag(tab.tag)

                Log.e("TabNavigator", "back f=$fragment")
                fragmentManager
                    .beginTransaction()
                    .remove(fragment!!)
                    .commit()
                tabController.remove(tab.tag)
                updateFragmentsState()
            }
        }
    }

    private fun replace(command: Replace) {
        val newScreen = command.screen as Screen
        createActivityIntent(activity, newScreen)?.also {
            checkAndStartActivity(it)
            activity.finish()
            return
        }


        tabController.findAlone(newScreen)?.also {
            val currentTag = tabController.getCurrent()?.tag.orEmpty()
            if (it.tag != currentTag) {
                val fragment = getByTag(currentTag)
                fragmentManager
                    .beginTransaction()
                    .remove(fragment!!)
                    .commit()
                tabController.remove(currentTag)
                tabController.setCurrent(it.tag)
                updateFragmentsState()
                return
            }
        }

        val newFragment = createFragment(newScreen)
        val tag = genTag()
        val fragment = getByTag(tabController.getCurrent()?.tag.orEmpty())
        Log.e("TabNavigator", "replace nf=$newFragment, of=$fragment")
        fragmentManager
            .beginTransaction()
            .remove(fragment!!)
            .add(containerId, newFragment, tag)
            .commit()
        tabController.replace(tag, newScreen)
        updateFragmentsState()
    }

    private fun backTo(command: BackTo) {
        val screen = requireNotNull(command.screen) {
            "TabNavigator does no support backTo null"
        }
        val tagsRemove = tabController.backTo(screen.screenKey)
        val transaction = fragmentManager.beginTransaction()
        Log.e("TabNavigator", "backTo tags=${tagsRemove.size}")
        tagsRemove.forEach {
            val fragment = getByTag(it)
            Log.e("TabNavigator", "backTo remove t=$fragment")
            transaction.remove(fragment!!)
        }
        transaction.commit()
        updateFragmentsState()
    }


    fun exit() {
        activity.finish()
    }

    private fun showSystemMessage(message: SystemMessage) {
        val text = when (message) {
            is SystemMessage.Text -> message.message
            is SystemMessage.Res -> activity.getString(message.res)
        }
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
    }

    private fun createActivityIntent(context: Context, screen: Screen): Intent? {
        when (screen) {
            is Screen.Main -> {
                return Intent(context, MainActivity::class.java)
            }

            is Screen.UpdateChecker -> {
                return Intent(context, UpdateCheckerActivity::class.java)
            }

            is Screen.ImageViewer -> {
                return Intent(context, ImageViewerActivity::class.java).apply {
                    putExtra(ImageViewerActivity.IMAGE_URLS_KEY, ArrayList<String>(screen.urls))
                    putExtra(ImageViewerActivity.SELECTED_INDEX_KEY, screen.selected)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            is Screen.Settings -> {
                return Intent(context, SettingsActivity::class.java).apply {
                    putExtra(SettingsActivity.ARG_NEW_PREFERENCE_SCREEN, screen.fragment)
                }
            }

            else -> {
                // do nothing
            }
        }
        return null
    }

    private fun checkAndStartActivity(activityIntent: Intent) {
        if (activityIntent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(activityIntent)
        }
    }

    private fun createFragment(screen: Screen): Fragment {
        return TabHelper.createTab(screen)
    }
}