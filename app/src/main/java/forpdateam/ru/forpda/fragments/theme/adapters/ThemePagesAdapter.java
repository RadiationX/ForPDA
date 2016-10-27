package forpdateam.ru.forpda.fragments.theme.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by radiationx on 26.10.16.
 */
public class ThemePagesAdapter extends BaseAdapter {
    private final String page = "Страница №";
    private final LayoutInflater inflater;
    private final int[] data;

    public ThemePagesAdapter(Context context, int[] data) {
        this.data = data;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int i) {
        return data[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.select_dialog_singlechoice, parent, false);
            holder = new ViewHolder();
            assert convertView != null;
            holder.text = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(page.concat(Integer.toString((int) getItem(position))));
        return convertView;
    }

    private class ViewHolder {
        public TextView text;
    }
}