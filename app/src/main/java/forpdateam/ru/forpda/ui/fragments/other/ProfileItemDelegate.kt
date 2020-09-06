package forpdateam.ru.forpda.ui.fragments.other

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.common.AuthState
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ProfileListItem
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.item_other_profile.view.*

class ProfileItemDelegate(
        private val clickListener: (ProfileModel?) -> Unit,
        private val logoutClickListener: () -> Unit
) : AdapterDelegate<MutableList<ListItem>>() {
    //private val dimensionsProvider = App.injections.dimensionsProvider
    private var compositeDisposable = CompositeDisposable()

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is ProfileListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as ProfileListItem
        (holder as ViewHolder).bind(item.profileItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_other_profile, parent, false),
            clickListener,
            logoutClickListener
    )

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder?) {
        super.onViewDetachedFromWindow(holder)
        compositeDisposable.dispose()
    }

    inner class ViewHolder(
            val view: View,
            private val clickListener: (ProfileModel?) -> Unit,
            private val logoutClickListener: () -> Unit
    ) : RecyclerView.ViewHolder(view) {

        private var item: ProfileModel? = null

        init {
            /*compositeDisposable.add(dimensionsProvider.observeDimensions().subscribe {
                view.setPadding(
                        view.paddingLeft,
                        it.statusBar,
                        view.paddingRight,
                        view.paddingBottom
                )
            })*/
            view.run {
                this.setOnClickListener { clickListener(item) }
                profileLogout.setOnClickListener { logoutClickListener() }
            }
        }

        fun bind(profileItem: ProfileModel?) {
            item = profileItem
            Log.e("S_DEF_LOG", "bind prfile " + profileItem)
            view.run {
                val imageUrl = profileItem?.avatar ?: "assets://av.png"
                ImageLoader.getInstance().displayImage(imageUrl, profileAvatar)

                if (profileItem != null) {
                    profileNick.text = profileItem.nick
                    profileDesc.text = "Перейти в профиль"
                    //profileLogout.visibility = View.VISIBLE
                } else {
                    profileNick.text = "Гость"
                    profileDesc.text = "Авторизоваться"
                    //profileLogout.visibility = View.GONE
                }
            }
        }
    }
}
