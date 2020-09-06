package forpdateam.ru.forpda.ui.fragments.devdb.device.specs;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils;

/**
 * Created by radiationx on 08.08.17.
 */

public class SpecsAdapter extends RecyclerView.Adapter<SpecsAdapter.ViewHolder> {
    private ArrayList<Pair<String, List<Pair<String, String>>>> list = new ArrayList<>();


    public void addAll(Collection<Pair<String, List<Pair<String, String>>>> results) {
        addAll(results, true);
    }

    public void addAll(Collection<Pair<String, List<Pair<String, String>>>> results, boolean clearList) {
        if (clearList)
            clear();
        list.addAll(results);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
    }

    @Override
    public SpecsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_spec_item, parent, false);
        return new SpecsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SpecsAdapter.ViewHolder holder, int position) {
        Pair<String, List<Pair<String, String>>> item = list.get(position);
        holder.title.setText(item.first);
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < item.second.size(); i++) {
            Pair<String, String> pair = item.second.get(i);
            String strColor = String.format("#%06X", 0xFFFFFF & App.getColorFromAttr(holder.itemView.getContext(), R.attr.second_text_color));
            builder.append("<small style=\"font-size:10px\"><span style=\"color: ").append(strColor).append("\">").append(pair.first).append("</span></small><br>").append(pair.second);
            if (i + 1 < item.second.size()) {
                builder.append("<br><br>");
            }
        }

        holder.desc.setText(ApiUtils.coloredFromHtml(builder.toString()));
        /*holder.price.setVisibility(item.getPrice() == null ? View.GONE : View.VISIBLE);
        if (item.getPrice() != null) {
            holder.price.setText(item.getPrice());
        }*/

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public Pair<String, List<Pair<String, String>>> getItem(int position) {
        return list.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView desc;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.item_title);
            desc = (TextView) v.findViewById(R.id.item_desc);
        }

    }
}
