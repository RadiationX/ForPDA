package forpdateam.ru.forpda.ui.views.pagination

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import forpdateam.ru.forpda.App.Companion.get
import forpdateam.ru.forpda.R
import java.util.Locale

/**
 * Created by radiationx on 26.10.16.
 */
class PaginationAdapter(context: Context?, private val data: IntArray) : BaseAdapter() {
    private val page = get().getString(R.string.pagination_page_number)
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(i: Int): Any {
        return data[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder

        if (convertView == null) {
            convertView =
                inflater.inflate(android.R.layout.simple_list_item_single_choice, parent, false)
            holder = ViewHolder()
            checkNotNull(convertView)
            holder.text = convertView.findViewById(android.R.id.text1)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }


        holder.text!!.text =
            String.format(Locale.getDefault(), page, data[position])
        return convertView
    }

    private inner class ViewHolder {
        var text: TextView? = null
    }
}