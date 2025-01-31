package forpdateam.ru.forpda.ui.fragments.other

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ProfileListItem
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.item_other_profile.view.profileAvatar
import kotlinx.android.synthetic.main.item_other_profile.view.profileDesc
import kotlinx.android.synthetic.main.item_other_profile.view.profileLogout
import kotlinx.android.synthetic.main.item_other_profile.view.profileNick

class ProfileItemDelegate(
    private val clickListener: (ForumUser?) -> Unit,
    private val logoutClickListener: () -> Unit
) : AdapterDelegate<MutableList<ListItem>>() {
    //private val dimensionsProvider = App.injections.dimensionsProvider
    private var compositeDisposable = CompositeDisposable()

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean =
        items[position] is ProfileListItem

    override fun onBindViewHolder(
        items: MutableList<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        val item = items[position] as ProfileListItem
        (holder as ViewHolder).bind(item.user)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
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
        private val clickListener: (ForumUser?) -> Unit,
        private val logoutClickListener: () -> Unit
    ) : RecyclerView.ViewHolder(view) {

        private var item: ForumUser? = null

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

        fun bind(user: ForumUser?) {
            item = user
            Log.e("S_DEF_LOG", "bind prfile " + user)
            view.run {
                val imageUrl = user?.avatar ?: "assets://av.png"
                ImageLoader.getInstance().displayImage(imageUrl, profileAvatar)

                if (user != null) {
                    profileNick.text = user.nick
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
