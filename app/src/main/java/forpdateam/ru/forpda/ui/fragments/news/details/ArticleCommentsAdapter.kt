package forpdateam.ru.forpda.ui.fragments.news.details

import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.App.Companion.getColorFromAttr
import forpdateam.ru.forpda.App.Companion.getVecDrawable
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.entity.remote.news.Comment.Karma
import forpdateam.ru.forpda.model.AuthHolder

/**
 * Created by radiationx on 03.09.17.
 */
class ArticleCommentsAdapter(
    private val authHolder: AuthHolder
) : RecyclerView.Adapter<ArticleCommentsAdapter.ViewHolder>() {
    private val list = ArrayList<Comment>()
    private var likedColorFilter: ColorFilter? = null
    private var dislikedColorFilter: ColorFilter? = null
    var clickListener: ClickListener? = null

    @JvmOverloads
    fun addAll(comments: List<Comment>, clearList: Boolean = true) {
        if (clearList) clear()
        list.addAll(comments)
        notifyDataSetChanged()
    }

    fun clear() {
        list.clear()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        likedColorFilter = PorterDuffColorFilter(
            getColorFromAttr(recyclerView.context, R.attr.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )
        dislikedColorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(
                recyclerView.context,
                R.color.dislike_color
            ), PorterDuff.Mode.SRC_ATOP
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.article_comment_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val karma = item.karma
        holder.content.text = item.content
        val authData = authHolder.get()
        if (item.isDeleted) {
            holder.itemView.isClickable = false
            if (holder.likeImage.visibility != View.GONE) {
                holder.likeImage.visibility = View.GONE
            }
            if (holder.likeCount.visibility != View.GONE) {
                holder.likeCount.visibility = View.GONE
            }
            if (holder.nick.visibility != View.GONE) {
                holder.nick.visibility = View.GONE
            }
            if (holder.date.visibility != View.GONE) {
                holder.date.visibility = View.GONE
            }
        } else {
            if (holder.likeImage.visibility != View.VISIBLE) {
                holder.likeImage.visibility = View.VISIBLE
            }
            if (holder.likeCount.visibility != View.VISIBLE) {
                holder.likeCount.visibility = View.VISIBLE
            }

            if (holder.nick.visibility != View.VISIBLE) {
                holder.nick.visibility = View.VISIBLE
            }
            if (holder.date.visibility != View.VISIBLE) {
                holder.date.visibility = View.VISIBLE
            }

            holder.nick.text = item.userNick
            holder.date.text = item.date

            if (karma!!.count == 0) {
                if (holder.likeCount.visibility != View.GONE) {
                    holder.likeCount.visibility = View.GONE
                }
            } else {
                if (holder.likeCount.visibility != View.VISIBLE) {
                    holder.likeCount.visibility = View.VISIBLE
                }
                holder.likeCount.text = karma.count.toString()
            }

            when (karma.status) {
                Karma.LIKED -> {
                    holder.likeImage.setImageDrawable(holder.heart)
                    holder.likeImage.colorFilter = likedColorFilter
                    holder.likeImage.isClickable = false
                }

                Karma.DISLIKED -> {
                    holder.likeImage.setImageDrawable(holder.heart_outline)
                    holder.likeImage.colorFilter = dislikedColorFilter
                    holder.likeImage.isClickable = false
                }

                Karma.NOT_LIKED -> {
                    holder.likeImage.setImageDrawable(holder.heart_outline)
                    holder.likeImage.clearColorFilter()
                    holder.likeImage.isClickable = authData.userId != item.userId
                }
            }
        }


        holder.itemView.setPadding(App.px12 * item.level, 0, 0, 0)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getItem(position: Int): Comment {
        return list[position]
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var content: TextView = v.findViewById(R.id.comment_content)
        var nick: TextView = v.findViewById(R.id.comment_nick)
        var date: TextView = v.findViewById(R.id.comment_date)
        var likeCount: TextView =
            v.findViewById(R.id.comment_like_count)
        var likeImage: ImageView =
            v.findViewById(R.id.comment_like_image)
        val heart: Drawable =
            getVecDrawable(v.context, R.drawable.ic_heart)
        val heart_outline: Drawable =
            getVecDrawable(v.context, R.drawable.ic_heart_outline)

        init {
            nick.setOnClickListener { v1: View? ->
                if (clickListener != null) {
                    clickListener!!.onNickClick(getItem(layoutPosition), layoutPosition)
                }
            }
            likeImage.setOnClickListener { v1: View? ->
                if (clickListener != null) {
                    clickListener!!.onLikeClick(getItem(layoutPosition), layoutPosition)
                }
            }
            content.setOnClickListener { v1: View? ->
                if (clickListener != null) {
                    clickListener!!.onReplyClick(getItem(layoutPosition), layoutPosition)
                }
            }
        }
    }

    interface ClickListener {
        fun onNickClick(comment: Comment, position: Int)

        fun onLikeClick(comment: Comment, position: Int)

        fun onReplyClick(comment: Comment, position: Int)
    }
}
