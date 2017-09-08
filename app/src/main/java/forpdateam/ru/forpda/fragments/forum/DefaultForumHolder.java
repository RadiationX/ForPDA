package forpdateam.ru.forpda.fragments.forum;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.forum.models.ForumItemTree;

/**
 * Created by radiationx on 28.02.17.
 */

public class DefaultForumHolder extends TreeNode.BaseNodeViewHolder<ForumItemTree> {
    TextView title;
    ImageView icon;
    ForumItemTree currentValue;

    public DefaultForumHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, ForumItemTree value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.forum_item_default, null, false);
        title = (TextView) view.findViewById(R.id.forum_item_title);
        icon = (ImageView) view.findViewById(R.id.forum_item_icon);

        currentValue = value;
        title.setText(value.getTitle());

        icon.setImageDrawable(App.getVecDrawable(context, value.getForums() == null ? R.drawable.ic_forum_go_to_topics : (node.isExpanded() ? R.drawable.ic_expand_less_black_24dp : R.drawable.ic_expand_more_black_24dp)));

        if (value.getForums() == null) {
            int suka = App.getDrawableResAttr(context, R.attr.count_background);
            icon.setBackgroundResource(suka);
        } else {
            icon.setBackground(null);
        }

        return view;
    }

    @Override
    public void toggle(boolean active) {
        if (currentValue.getForums() != null) {
            icon.setRotationY(active ? 1f : 0f);
            icon.setImageDrawable(App.getVecDrawable(context, active ? R.drawable.ic_expand_less_black_24dp : R.drawable.ic_expand_more_black_24dp));
        }
    }
}
