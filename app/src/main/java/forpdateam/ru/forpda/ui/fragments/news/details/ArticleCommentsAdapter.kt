package forpdateam.ru.forpda.ui.fragments.news.details

import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.ArticleCommentItemBinding
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.entity.remote.news.Comment.Karma
import forpdateam.ru.forpda.extensions.getColorFromAttr
import forpdateam.ru.forpda.extensions.getDimenPx
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
            recyclerView.context.getColorFromAttr(androidx.appcompat.R.attr.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )
        dislikedColorFilter = PorterDuffColorFilter(
            recyclerView.context.getColor(R.color.dislike_color),
            PorterDuff.Mode.SRC_ATOP
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.article_comment_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getItem(position: Int): Comment {
        return list[position]
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val binding by viewBinding<ArticleCommentItemBinding>()

        init {
            binding.commentNick.setOnClickListener { v1: View? ->
                if (clickListener != null) {
                    clickListener!!.onNickClick(getItem(layoutPosition), layoutPosition)
                }
            }
            binding.commentLikeImage.setOnClickListener { v1: View? ->
                if (clickListener != null) {
                    clickListener!!.onLikeClick(getItem(layoutPosition), layoutPosition)
                }
            }
            binding.commentContent.setOnClickListener { v1: View? ->
                if (clickListener != null) {
                    clickListener!!.onReplyClick(getItem(layoutPosition), layoutPosition)
                }
            }
        }

        fun bind(item: Comment) {
            val karma = item.karma
            binding.commentContent.text = item.content
            val authData = authHolder.get()
            if (item.isDeleted) {
                binding.root.isClickable = false
                binding.commentLikeImage.visibility = View.GONE
                binding.commentLikeCount.visibility = View.GONE
                binding.commentNick.visibility = View.GONE
                binding.commentDate.visibility = View.GONE
            } else {
                binding.commentLikeImage.visibility = View.VISIBLE
                binding.commentLikeCount.visibility = View.VISIBLE

                binding.commentNick.visibility = View.VISIBLE
                binding.commentDate.visibility = View.VISIBLE

                binding.commentNick.text = item.user.nick
                binding.commentDate.text = item.date

                if (karma!!.count == 0) {
                    binding.commentLikeCount.visibility = View.GONE
                } else {
                    binding.commentLikeCount.visibility = View.VISIBLE
                    binding.commentLikeCount.text = karma.count.toString()
                }

                when (karma.status) {
                    Karma.LIKED -> {
                        binding.commentLikeImage.setImageResource(R.drawable.ic_heart)
                        binding.commentLikeImage.colorFilter = likedColorFilter
                        binding.commentLikeImage.isClickable = false
                    }

                    Karma.DISLIKED -> {
                        binding.commentLikeImage.setImageResource(R.drawable.ic_heart_outline)
                        binding.commentLikeImage.colorFilter = dislikedColorFilter
                        binding.commentLikeImage.isClickable = false
                    }

                    Karma.NOT_LIKED -> {
                        binding.commentLikeImage.setImageResource(R.drawable.ic_heart_outline)
                        binding.commentLikeImage.clearColorFilter()
                        binding.commentLikeImage.isClickable = authData.userId != item.user.id
                    }
                }
            }


            binding.root.setPadding(binding.root.context.getDimenPx(R.dimen.dp12) * item.level, 0, 0, 0)
        }
    }

    interface ClickListener {
        fun onNickClick(comment: Comment, position: Int)

        fun onLikeClick(comment: Comment, position: Int)

        fun onReplyClick(comment: Comment, position: Int)
    }
}
