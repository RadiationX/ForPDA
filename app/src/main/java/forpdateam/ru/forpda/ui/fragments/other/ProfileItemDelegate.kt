package forpdateam.ru.forpda.ui.fragments.other

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.ItemOtherProfileBinding
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ProfileListItem

class ProfileItemDelegate(
    private val clickListener: (ForumUser?) -> Unit,
    private val logoutClickListener: () -> Unit
) : AdapterDelegate<List<ListItem>>() {

    override fun isForViewType(items: List<ListItem>, position: Int): Boolean =
        items[position] is ProfileListItem

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: List<Any>
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

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
    }

    inner class ViewHolder(
        val view: View,
        private val clickListener: (ForumUser?) -> Unit,
        private val logoutClickListener: () -> Unit
    ) : RecyclerView.ViewHolder(view) {

        private val binding by viewBinding<ItemOtherProfileBinding>()

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
                binding.profileLogout.setOnClickListener { logoutClickListener() }
            }
        }

        fun bind(user: ForumUser?) {
            item = user
            Log.e("S_DEF_LOG", "bind prfile " + user)
            view.run {
                val imageUrl = user?.avatar ?: "assets://av.png"
                ImageLoader.getInstance().displayImage(imageUrl, binding.profileAvatar)

                if (user != null) {
                    binding.profileNick.text = user.nick
                    binding.profileDesc.text = "Перейти в профиль"
                    //profileLogout.visibility = View.VISIBLE
                } else {
                    binding.profileNick.text = "Гость"
                    binding.profileDesc.text = "Авторизоваться"
                    //profileLogout.visibility = View.GONE
                }
            }
        }
    }
}
